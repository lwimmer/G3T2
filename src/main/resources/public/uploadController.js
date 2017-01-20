(function () {

    app.controller("UploadController", ["$http", "$scope", "$window", "$localStorage", "ngDialog", "fileUpload", "cfpLoadingBar", 
    	function($http, $scope, $window, $localStorage, ngDialog, fileUpload, cfpLoadingBar) {
    	
    	$scope.putraid = "RAID1";
    	
        $scope.cancel = function() {
        	$scope.closeThisDialog();
        }
        
        $scope.uploadFile = function(){
            var file = $scope.myFile;
           
            var putraid = $scope.putraid;
            var uploadUrl = "/file/"+file.name;
            
            $http.put(uploadUrl, file, { params: { raid: putraid } }).then(function(response) {
                $scope.files = response.data;
                $window.location.href = '/files.html';
            }, function errorCallback(response) {
            	alert("Upload failed!");
            	console.log(response.data);
            }).finally(function() {
                cfpLoadingBar.complete();
            });
            
            
         };
        
    } ]); 

})();

