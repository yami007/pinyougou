 //控制层 
app.controller('itemCatController' ,function($scope,$controller  ,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
    //保存
    $scope.save=function(){
        var serviceObject;//服务层对象
        if($scope.entity.id!=null&&$scope.entity.id!=0){//如果有ID
            serviceObject=itemCatService.update($scope.entity); //修改
        }else{
            $scope.entity.parentId=$scope.parentid;
            serviceObject=itemCatService.add( $scope.entity);//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    //重新查询
                    itemCatService.findByParentId($scope.parentid).success(
                        function (response) {
                            $scope.list=response;
                        });
                }else{
                    alert(response.message);
                }
            }
        );
    }
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
                    //重新查询
                    itemCatService.findByParentId($scope.parentid).success(
                        function (response) {
                            $scope.list=response;
                        }
                    );
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//按照父id查找
    $scope.flag=1;
	$scope.findByParentId=function (entity) {
        $scope.parentid=entity.id;
        if($scope.flag==1){
            $scope.entity_1={id:0,name:'顶级分类目录'};
            $scope.entity_2=null;
            $scope.entity_3=null;
        }
        if($scope.flag==2){
            $scope.entity_2=entity;
            $scope.entity_3=null;
        }
        if($scope.flag==3){
            $scope.entity_3=entity;
        }
        itemCatService.findByParentId(entity.id).success(
            function (response) {
                $scope.list=response;
            })
        }
    $scope.setflag=function (value) {
        $scope.flag=value;
    }

    $scope.gettypelist=function () {
        typeTemplateService.findAll().success(   // 调用服务方法获取所有数据
            function (response) {
                for(var i=0;i<response.length;i++){
                    var entity=response[i];
                    $("#typeselect").append("<option value="+entity.id+">"+entity.name+"</option>");
                }
            })
    }
    
});	
