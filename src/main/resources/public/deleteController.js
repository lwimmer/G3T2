(function () {

    app.controller("DeleteController", ["$http", "$scope", "$window", "$localStorage", "ngDialog", function($http, $scope, $window, $localStorage, ngDialog) {
    	
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
            });
        	$window.location.href = '/files.html';
        };
        
    } ]); 

})();

