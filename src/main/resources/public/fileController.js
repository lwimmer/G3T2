(function () {

    app.controller("FileController", ["$http", "$scope", "giveFile", "$window", "$localStorage", "ngDialog", function($http, $scope, giveFile, $window, $localStorage, ngDialog) {
    	
        $http.get("/file").then(function(response) {
            $scope.files = response.data;
        });
        
        $scope.goDetail = function(file) {
        	giveFile.setFilename(file);
        	$localStorage.filename = file;
        	$window.location.href = '/detail.html';
        }
        
        $scope.openDelete = function(file) {
        	$localStorage.filename = file;
        	ngDialog.open({ template: 'delete.html', className: 'ngdialog-theme-default', controller: 'DeleteController', scope: $scope });
        };
        
        $scope.openUpload = function(file) {
        	ngDialog.open({ template: 'upload.html', className: 'ngdialog-theme-default', controller: 'UploadController', scope: $scope });
        };
        
    } ]); 

})();

