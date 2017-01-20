(function () {

    app.controller("FileController", ["$http", "$scope", "giveFile", "$window", "$localStorage", "ngDialog", function($http, $scope, giveFile, $window, $localStorage, ngDialog) {
    	
        $http.get("/file").then(function(response) {
            $scope.files = response.data;
        });
        
        $scope.getDownload = function(file) {
        	var URL = '/file/'+file;
        	
            $http.get(URL, {
            	dataType : "binary",
                processData : false,
                responseType : 'arraybuffer'
            })
            .then(function(response) {
            	var downloadFile = response.data;
            	var blob = new Blob([response.data], {type: "application/octet-stream"});
            	var blobURL = window.URL.createObjectURL(blob);
            	if (window.navigator.msSaveOrOpenBlob) {
            	    // of course, IE needs special hand-holding....
            	    window.navigator.msSaveOrOpenBlob(blob, 'something.pdf');
            	} else {
            	    window.open(blobURL);
            	}
            });
        }
        
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

