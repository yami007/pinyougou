//首页控制器
app.controller('indexController',function($scope,loginService,addressService){
    $scope.showName=function(){
        loginService.showName().success(
            function(response){
                $scope.loginName=response.loginName;
            }
        );
    }
    //查询地址列表
    $scope.findListByUserId = function () {
        addressService.findListByUserId().success(
            function (response) {
                $scope.addressList = response;
            }
        );
    }
    $scope.address={}
    //新增地址
    $scope.addAddress=function(){
        if($scope.address.isDefault==true){
            $scope.address.isDefault='1'
        }else{
            $scope.address.isDefault='0'
        }
        addressService.addAdress($scope.address).success(
            function (response) {
                alert(response.message);
                $scope.findListByUserId()
            }
        )
    }

    //填入别名
    $scope.checkAlias=function (alias) {
        $scope.address.alias=alias
    }

});
