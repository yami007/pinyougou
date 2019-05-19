app.service('addressService',function($http){
    //购物车列表
    this.findListByUserId=function(){
        return $http.get('adress/findListByUserId.do');
    }
});
