(function () {

  //  var app = angular.module("app", []);

    app.controller("DetailController", ["$http", "$scope", "giveFile", "$window", "$localStorage", function($http, $scope, giveFile, $window, $localStorage) {
    	
    	var filename = $localStorage.filename; 
    	
        $scope.filename = filename;
        
        var URL = '/file/'+filename;
    	
        $http.get(URL, {
        })
        .then(function(response) {
            $scope.file = response.data;
        });
        
        console.log($scope.file);
        
        $scope.goBack = function() {
        	$window.location.href = '/files.html';
        }
        
    } ]); 

})();