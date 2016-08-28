(function () {
    'use strict';
    var module = angular.module('myApp');

    module.factory('WateringProfileService', ['$http', '$q', '$log', function ($http, $q, $log) {
            return {
                getMinMaxValues: function () {
                    return $http.get('wateringprofile/minmax').then(
                        function (response) {
                            return response.data;
                        },
                        function (errResponse) {
                            console.error('Error while fetchingService user profile!!!');
                            return $q.reject(errResponse);
                        }
                    );
                }
                //,saveWateringProfile: function (userprofiledata) {
                //    return $http.post('userprofile', userprofiledata).then(
                //        function (response) {
                //            return response.data;
                //        },
                //        function (errResponse) {
                //            console.error('Error while saving user profile!!!');
                //            return $q.reject(errResponse);
                //        }
                //    );
                //}

            }
        }]
    );

}());