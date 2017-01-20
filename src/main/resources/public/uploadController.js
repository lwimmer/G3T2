(function () {

    app.controller("UploadController", ["$http", "$scope", "$window", "$localStorage", "ngDialog", "fileUpload", function($http, $scope, $window, $localStorage, ngDialog, fileUpload) {
    	
        $scope.cancel = function() {
        	$scope.closeThisDialog();
        }
        
        $scope.uploadFile = function(){
            var file = $scope.myFile;
           
            var uploadUrl = "/file/"+file.name;
            
            $http.put(uploadUrl, file).then(function(response) {
                $scope.files = response.data;
            });
            
            $window.location.href = '/files.html';
         };
        
    } ]); 

})();

