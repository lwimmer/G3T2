(function() {
    'use strict';

    angular
        .module('app')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('file', {
            url: '/files.html',
            views: {
                'content@': {
                    templateUrl: 'files.html',
                    controller: 'FileController',
                    controllerAs: 'vm'
                }
            },
        })
        .state('detail', {
            parent: 'file',
            url: '/detail.html',
            views: {
                'content@': {
                    templateUrl: 'detail.html',
                    controller: 'DetailController',
                    controllerAs: 'vm'
                }
            },
        });

    }

})();
