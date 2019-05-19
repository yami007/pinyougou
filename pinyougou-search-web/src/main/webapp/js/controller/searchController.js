app.controller('searchController',function ($scope,$location,searchService) {
    //写一个方法 当点击搜索的按钮的时候要调用发送请求将数据查询出来 展示到页面中

    $scope.searchMap={'keywords':'三星','category':'','brand':'','spec':{},'price':'','pageNum':1,'pageSize':40,'sortField':'','sort':'' };
    $scope.cleara=function () {
        $scope.searchMap = {'keywords': $scope.searchMap.keywords, 'category': '', 'brand': '','price':'', 'spec':{},'pageNum':1,'pageSize':40,'sortField':'','sort':'' };
    }
    $scope.search=function () {
        $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNum) ;
        searchService.search($scope.searchMap).success(
            function (response) {//Map
                $scope.resultMap=response;
                buildPageLabel();
            }
        )
    }
    //添加搜索项
    $scope.addSearchItem=function(key,value){
        if(key=='category' || key=='brand' ||key=='price'){//如果点击的是分类或者是品牌
            $scope.searchMap[key]=value;
        }else{
            $scope.searchMap.spec[key]=value;
        }
        $scope.search();//执行搜索
    }
    //移除复合搜索条件
    $scope.removeSearchItem=function(key){
        if(key=="category" ||  key=="brand" ||key=='price'){//如果是分类或品牌
            $scope.searchMap[key]="";
        }else{//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }
        $scope.search();//执行搜索
    }

    //构建分页标签(totalPages为总页数)
    buildPageLabel=function(){
        //构建分页栏
        $scope.pageLabel=[];
        var firstPage=1;//开始页码
        var lastPage=$scope.resultMap.totalPage;//截止页码
        $scope.firstDot=true;//前面有点
        $scope.lastDot=true;//后边有点
        if($scope.resultMap.totalPage>5){  //如果页码数量大于5
            if($scope.searchMap.pageNum<=3){//如果当前页码小于等于3 ，显示前5页
                lastPage=5;
                $scope.firstDot=false;//前面没点
            }else if( $scope.searchMap.pageNum>= $scope.resultMap.totalPage-2 ){//显示后5页
                firstPage=$scope.resultMap.totalPage-4;
                $scope.lastDot=false;//后边没点
            }else{  //显示以当前页为中心的5页
                firstPage=$scope.searchMap.pageNum-2;
                lastPage=$scope.searchMap.pageNum+2;
            }
        }else{
            $scope.firstDot=false;//前面无点
            $scope.lastDot=false;//后边无点
        }
        //构建页码
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }


    $scope.queryByPage=function (pageNo) {
        if(pageNo<1 || pageNo>$scope.resultMap.totalPage){
            return;
        }

        if(!isNaN(pageNo)){
            $scope.searchMap.pageNum=pageNo;
        }else{
            alert("请输入数字！！")
        }
        $scope.search();
    }

    $scope.sortSearch=function(sortField,sort){
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        $scope.search();
    }
    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                $scope.searchMap.brand=$scope.resultMap.brandList[i].text;
                return true;
            }
        }
        return false;
    }

    $scope.loadkeywords=function(){
        $scope.searchMap.keywords=  $location.search()['keywords'];
        $scope.search();
    }




})