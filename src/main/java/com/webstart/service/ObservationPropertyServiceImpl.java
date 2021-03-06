package com.webstart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstart.DTO.*;
import com.webstart.Enums.FeatureTypeEnum;
import com.webstart.Enums.StatusTimeConverterEnum;
import com.webstart.Helpers.HelperCls;
import com.webstart.model.*;
import com.webstart.repository.FeatureofinterestJpaRepository;
import com.webstart.repository.ObservablePropertyJpaRepository;
import com.webstart.repository.ObservationJpaRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;


@Service("observationProperyService")
@Transactional
public class ObservationPropertyServiceImpl implements ObservationProperyService {

    @Autowired
    ObservablePropertyJpaRepository observablePropertyJpaRepository;
    @Autowired
    ObservationJpaRepository observationJpaRepository;
    @Autowired
    FeatureofinterestJpaRepository featureofinterestJpaRepository;
    //
    private final Logger logger =   LoggerFactory.getLogger(HelperCls.class);

    public JSONObject getAllObsPropeties() {

        JSONObject finalobj = new JSONObject();
        JSONArray list = new JSONArray();

        List<ObservableProperty> obsPropertiesList = observablePropertyJpaRepository.findAllExceptWatering();

        for (ObservableProperty observableProperty : obsPropertiesList) {
            JSONObject obj = new JSONObject();
            obj.put("observablepropertyid", observableProperty.getObservablePropertyId());
            obj.put("description", observableProperty.getDescription());
            list.add(obj);
        }

        finalobj.put("obsprop", list);

        return finalobj;
    }

    public WateringMeasure getWateringData(int userId, String identifier, DateTime from, DateTime to) {
        WateringMeasure wateringMeasure = new WateringMeasure();

        try {
            int offset =  DateTimeZone.getDefault().getOffset(new Instant());
            java.sql.Timestamp timeFrom = new Timestamp(from.getMillis() - offset);
            java.sql.Timestamp timeTo = new Timestamp(to.getMillis() - offset);
            logger.debug("getWateringData() params: datetimeFrom={}, timestampFrom={}", from, new Timestamp(from.getMillis() - offset));
            logger.debug("getWateringData() params: datetimeTo={}, timestampTo={}", to, new Timestamp(to.getMillis() - offset));

            Featureofinterest featureofinterest = featureofinterestJpaRepository.getFeatureofinterestByIdentifier(identifier);
            //
            List<Object[]> listofObjs = observationJpaRepository.findWateringMeasures(userId, identifier, timeFrom, timeTo);

            if (listofObjs.size() == 0) {
                return null;
            }

            Object[] obj = listofObjs.get(0);
            wateringMeasure.setIdentifier(String.valueOf(listofObjs.get(0)[0]));
            wateringMeasure.setObservableProperty(String.valueOf(listofObjs.get(0)[1]));
            wateringMeasure.setUnit(String.valueOf(listofObjs.get(0)[5]));
            List<WateringValueTime> ls = new ArrayList<WateringValueTime>();

            for (Object[] objValueTime : listofObjs) {
                DateTimeFormatter dtfInput = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
                HelperCls.ConvertToDateTime convertable = new HelperCls.ConvertToDateTime();
                DateTime dtfrom = convertable.GetUTCDateTime(objValueTime[2].toString(), dtfInput, featureofinterest.getTimezone(), StatusTimeConverterEnum.TO_TIMEZONE);
                DateTime dtuntil = convertable.GetUTCDateTime(objValueTime[3].toString(), dtfInput, featureofinterest.getTimezone(), StatusTimeConverterEnum.TO_TIMEZONE);
                //substract the default timezone pc offset in milliseconds
                logger.debug("getWateringData() params: dtfrom={}, offset={} timestamp={}", dtfrom, offset, new Timestamp(dtfrom.getMillis() - offset));
                logger.debug("getWateringData() params: dtuntil={}, offset={} timestamp={}", dtuntil, offset, new Timestamp(dtuntil.getMillis() - offset));
                ls.add(new WateringValueTime((BigDecimal) objValueTime[4], new Timestamp(dtfrom.getMillis() - offset), new Timestamp(dtuntil.getMillis() - offset)));
            }

            wateringMeasure.setMeasuredata(ls);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return wateringMeasure;
    }

    public Long getObservationsCounter(Long obspropId, int userId, String identifier, DateTime from, DateTime to) {
        int offset =  DateTimeZone.getDefault().getOffset(new Instant());
        //
        return observationJpaRepository.findMeasuresCount(obspropId, userId, identifier, new java.sql.Timestamp(from.getMillis() - offset), new java.sql.Timestamp(to.getMillis() - offset));
    }

    public ObservableMeasure getObservationData(Long obspropId, int userId, String identifier, DateTime from, DateTime to) {
        ObservableMeasure obsMeasure = new ObservableMeasure();

        try {
            Featureofinterest featureofinterest = featureofinterestJpaRepository.getFeatureofinterestByIdentifier(identifier);
            //
            int offset =  TimeZone.getDefault().getRawOffset();
            logger.debug("getObservationData() params: datetimeFrom={} timestampFrom={}", from, new Timestamp(from.getMillis() - offset));
            logger.debug("getObservationData() params: datetimeTo={} timestampTo={}", to, new Timestamp(to.getMillis() - offset));
            List<Object[]> listofObjs = observationJpaRepository.findMeasureByObsPropId(obspropId, userId, identifier, new Timestamp(from.getMillis() - offset), new Timestamp(to.getMillis() - offset));

            if (listofObjs.size() == 0) {
                return null;
            }

//            int offset =  DateTimeZone.getDefault().getOffset(new Instant());

            Object[] obj = listofObjs.get(0);
            obsMeasure.setIdentifier(String.valueOf(obj[0]));
            obsMeasure.setObservableProperty(String.valueOf(obj[1]));
            obsMeasure.setUnit(String.valueOf(obj[4]));

            List<ValueTime> ls = new ArrayList<ValueTime>();

            for (Object[] objec : listofObjs) {
                DateTimeFormatter dtfInput = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
                HelperCls.ConvertToDateTime convertable = new HelperCls.ConvertToDateTime();
                DateTime dt = convertable.GetUTCDateTime(objec[2].toString(), dtfInput, featureofinterest.getTimezone(), StatusTimeConverterEnum.TO_TIMEZONE);
                Timestamp tTime = new Timestamp(dt.getMillis() - offset);
                logger.debug("getObservationData() params: datetime={}, offset={} timestamp={}", dt, offset, tTime);
                ls.add(new ValueTime(tTime.getTime()/1000L, (BigDecimal) objec[3], tTime));
            }

            obsMeasure.setMeasuredata(ls);
        } catch (Exception exc) {
            exc.printStackTrace();
            return null;
        }

        return obsMeasure;
    }

    public String getLastObservationsDate(int userId) {
        String jsonInString;

        try {
            Timestamp lastdate = observationJpaRepository.findlastdatetime(userId);
            Featureofinterest featureofinterest = featureofinterestJpaRepository.getAllByUseridAndFeatureofinteresttypeid(userId, (long) FeatureTypeEnum.STATION.getValue()).get(0);

            //TODO create a function with timestamp
            //TimeZone
            DateTimeZone tz = DateTimeZone.forID(featureofinterest.getTimezone());

            //Convert time to UTC
            int offset = tz.getOffset(new DateTime());
            Calendar cal = Calendar.getInstance();

            cal.setTimeInMillis(lastdate.getTime());
            cal.add(Calendar.MILLISECOND, offset);
            lastdate = new Timestamp(cal.getTime().getTime());

            ObjectMapper mapper = new ObjectMapper();
            jsonInString = mapper.writeValueAsString(lastdate);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return jsonInString;
    }


    public List<ObservationMeasure> getLastObservationbyIdentifier(int userId, String identifier) {
        //String jsonInString = null;
        List<ObservationMeasure> ls = new ArrayList<ObservationMeasure>();

        try {
            Featureofinterest featureofinterest = featureofinterestJpaRepository.getFeatureofinterestByIdentifier(identifier);
            //
            Timestamp lastdate = observationJpaRepository.findlastdatetime(userId, identifier);
            List<Object[]> listMeasures = observationJpaRepository.findLastMeasures(userId, identifier, lastdate);

            if (listMeasures.size() == 0) {
                return null;
            }

            ls = new ArrayList<ObservationMeasure>();
            Iterator itr = listMeasures.iterator();

            while (itr.hasNext()) {
                Object[] obj = (Object[]) itr.next();

                DateTimeFormatter dtfInput = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
                HelperCls.ConvertToDateTime convertable = new HelperCls.ConvertToDateTime();
                DateTime dt = convertable.GetUTCDateTime(obj[1].toString(), dtfInput, featureofinterest.getTimezone(), StatusTimeConverterEnum.TO_TIMEZONE);
                Timestamp tTime = new Timestamp(dt.getMillis());
                ls.add(new ObservationMeasure((tTime.getTime()) / 1000L, (BigDecimal) obj[2], tTime, obj[3].toString(), obj[0].toString()));
            }

            //ObjectMapper mapper = new ObjectMapper();       //Object to JSON in String
            //jsonInString = mapper.writeValueAsString(ls);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return ls;
    }

    public AutomaticWater getLastWateringObsbyIdentifier(int userId, String identifier) {
        AutomaticWater automaticWater = null;

        try {
            Featureofinterest featureofinterest = featureofinterestJpaRepository.getFeatureofinterestByIdentifier(identifier);
            //
            Timestamp lastdate = observationJpaRepository.findWateringlastdatetime(userId, identifier);
            if(lastdate == null) {
                return null;
            }

            List<Object[]> listMeasures = observationJpaRepository.findLastWateringMeasures(userId, identifier, lastdate);

            if (listMeasures.size() == 0) {
                throw new Exception("Error: Last measure cannot found");
            }

            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

            Iterator itr = listMeasures.iterator();
            while (itr.hasNext()) {
                Object[] obj = (Object[]) itr.next();
                DateTimeFormatter dtfInput = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
                HelperCls.ConvertToDateTime convertable = new HelperCls.ConvertToDateTime();
                DateTime dtfrom = convertable.GetUTCDateTime(obj[1].toString(), dtfInput, featureofinterest.getTimezone(), StatusTimeConverterEnum.TO_TIMEZONE);
                DateTime dtuntil = convertable.GetUTCDateTime(obj[2].toString(), dtfInput, featureofinterest.getTimezone(), StatusTimeConverterEnum.TO_TIMEZONE);
                automaticWater = new AutomaticWater(fmt.print(dtfrom), fmt.print(dtuntil), (BigDecimal) obj[3], identifier);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        return automaticWater;
    }

    public void setObservationMinmaxValues(List<FeatureMinMaxValue> observationMinmaxList) {
        try {
            for (FeatureMinMaxValue featureMinMaxValue : observationMinmaxList) {
                featureofinterestJpaRepository.setObservableMinmax(featureMinMaxValue.getObspropertyid(), featureMinMaxValue.getMinval(), featureMinMaxValue.getMaxval());
            }
        } catch (Exception exc) {

        }
    }


}


