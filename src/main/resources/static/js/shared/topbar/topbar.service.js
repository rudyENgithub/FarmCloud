(function () {
    'use strict';
    var module = angular.module("myApp");

    module.factory('userService', [
        '$http', '$q', '$log', function ($http, $q, $log) {

            return {
                getuser: function () {
                    return $http.get('topbaruser').then(
                        function (response) {
                            return response.data;
                        }, function (errResponse) {
                            console.error('Error while userService user');
                            return $q.reject(errResponse);
                        }
                    );
                },
                getnotifcounter: function () {
                    return $http.get('user/notifications').then(
                        function (response) {
                            return response.data;
                        }, function (errResponse) {
                            console.error('Error while userService notifications');
                            return $q.reject(errResponse);
                        }
                    );
                },
                setNotificationRead: function (notificationid) {
                    return $http.put(`user/notifications/${notificationid}/read`).then(
                        function (response) {
                            return response.data;
                        }, function (errResponse) {
                            console.error('Error while userService notifications');
                            return $q.reject(errResponse);
                        }
                    );
                },setAllNotificationRead: function () {
                    return $http.put('user/notifications/all/read').then(
                        function (response) {
                            return response.data;
                        }, function (errResponse) {
                            console.error('Error while userService notifications');
                            return $q.reject(errResponse);
                        }
                    );
                }
            }
        }
    ]);
}());
