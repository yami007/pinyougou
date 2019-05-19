 //控制层 
app.controller('goodsController' ,function($scope,$controller   ,goodsService,itemCatService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					alert(response.message)
                    //清空选中的商品
                    $scope.selectIds=[];
					$scope.reloadList();//刷新列表
				}else{
                    alert(response.message)
				}
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){
        $scope.searchEntity.auditStatus=0;
        $scope.searchEntity.isDelete=0;
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

    $scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态
	//获取所有的的分类
	$scope.itemcatArry=[];
	$scope.getItemCat=function () {
		itemCatService.findAll().success(
			function (response) {
                for (var i=0;i<response.length;i++) {
                    var itemcat= response[i];
                    $scope.itemcatArry.push(itemcat.name)
                }

        })
    }

    //更改商品审核状态
	$scope.updatestatus=function (statusnum) {
		if($scope.selectIds.length==0){
			alert("请选择商品")
			return false;
		}
        goodsService.updatestatus($scope.selectIds,statusnum).success(
        	function (response) {
                if(response.success){
                    //提示
                	alert(response.message);
                    //清空选中的商品
                    $scope.selectIds=[];
                    //重新查询
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
        })
    }
});	
