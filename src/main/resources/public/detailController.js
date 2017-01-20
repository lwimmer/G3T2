(function () {

  //  var app = angular.module("app", []);

    app.controller("DetailController", ["$http", "$scope", "giveFile", "$window", "$localStorage", function($http, $scope, giveFile, $window, $localStorage) {
    	
    	var filename = $localStorage.filename; 
    	
        $scope.filename = filename;
        
        var URL = '/file/'+filename+'/metadata';
    	
        $http.get(URL, {
        })
        .then(function(response) {
        	console.log("in then");
        	console.log(response.data);
            $scope.versions = response.data;
            var str = response.data.locations[0].filename;
            $scope.name = str.split("_");
        }, function errorCallback(response) {
        	alert("Reading versions failed");
        	console.log(response.data);
         });
        
        console.log($scope.versions);
        
        $scope.goBack = function() {
        	$window.location.href = '/files.html';
        }
        
    } ]); 

})();