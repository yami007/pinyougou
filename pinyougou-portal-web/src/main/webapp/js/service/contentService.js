app.service('contentService',function($http) {
    this.findByCategory = function (categoryId) {
        return $http.get('../content/findByCategory.do?categoryId=' + categoryId);
    }
});