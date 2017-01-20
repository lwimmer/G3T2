(function () {

    app.controller("DeleteController", ["$http", "$scope", "$window", "$localStorage", "ngDialog", "cfpLoadingBar",
    	function($http, $scope, $window, $localStorage, ngDialog, cfpLoadingBar) {
    	
    	var filename = $localStorage.filename;
    	$scope.filename = filename;
    	
    	var URL = '/file/'+filename;

        $scope.cancel = function() {
        	$localStorage.filename = '';
        	$scope.closeThisDialog();
        }
        
        $scope.deleteFile = function() {
        	$http.delete(URL).then(function(response) {
                $scope.files = response.data;
                $window.location.href = '/files.html';
            }, function errorCallback(response) {
            	alert("Delete failed!");
            	console.log(response.data);
            }).finally(function() {
                cfpLoadingBar.complete();
            });
        	
        };
        
    } ]); 

})();

