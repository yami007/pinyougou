 //控制层 
app.controller('goodsController' ,function($scope,$controller ,goodsService,uploadService,itemCatService,typeTemplateService){
	
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
	
	//添加商品
	$scope.add=function(){
        $scope.entity.tbGoodsDesc.introduction=editor.html();
		goodsService.add($scope.entity).success(
			function(response){
				if(response.success){
                    alert(response.message);
                    $scope.entity={};
                    editor.html('');
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
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//上传文件
	$scope.upload=function () {
		uploadService.uploadFile().success(
			function (response) {
			if(response.success){
                $scope.image_entity.url=response.message;//设置文件地址
			}else
				alert(response.message)
        }).error(function () {
			alert("上传发生错误")
        })
    }

    //增加图片
    // $scope.entity={tbGoods:{},tbGoodsDesc:{itemImages:[]}};//定义页面实体结构
    $scope.add_image_entity=function () {
        $scope.entity.tbGoodsDesc.itemImages.push($scope.image_entity);
    }

    //移除图片
   $scope.remove_image_entity=function (index) {
       $scope.entity.tbGoodsDesc.itemImages.splice(index,1);
   }

    //根据父一级分类
    $scope.findByParentId=function () {
        itemCatService.findByparernid(0).success(
            function (response) {
                $scope.itemcat1list=response;
            })
    }

    // 读取二级分类
    $scope.$watch('entity.tbGoods.category1Id', function(newValue, oldValue) {
        //根据选择的值，查询二级分类
        itemCatService.findByparernid(newValue).success(
            function(response){
                $scope.itemCat2List=response;
            }
        );
    });
    // 读取三级分类
    $scope.$watch('entity.tbGoods.category2Id', function(newValue, oldValue) {
        //根据选择的值，查询三级分类
        itemCatService.findByparernid(newValue).success(
            function(response){
                $scope.itemCat3List=response;
            }
        );
    });
    // 读取获得模板id
    $scope.$watch('entity.tbGoods.category3Id', function(newValue, oldValue) {

        itemCatService.findOne(newValue).success(
            function(response){
                $scope.entity.tbGoods.typeTemplateId=response.typeId;
            }
        );
    });
    //模板ID选择后  更新品牌列表
    $scope.$watch('entity.tbGoods.typeTemplateId', function(newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function(response){
                $scope.typeTemplate=response;//获取类型模板
                $scope.typeTemplate.brandIds= JSON.parse( $scope.typeTemplate.brandIds);//品牌列表
                $scope.entity.tbGoodsDesc.customAttributeItems=JSON.parse( $scope.typeTemplate.customAttributeItems);//扩展属性
            }
        );
        typeTemplateService.findSpecList(newValue).success(
        	function (response) {
				$scope.speclist=response;
        })
    });

	// 保存用户选择的规格及其选项
    $scope.entity={tbGoods:{},tbGoodsDesc:{itemImages:[],specificationItems:[]}};
    $scope.updateSpecAttribute=function($event,name,value){
        var object= $scope.searchObjectByKey(
            $scope.entity.tbGoodsDesc.specificationItems ,'attributeName', name);
        if(object!=null){
            if($event.target.checked){
                object.attributeValue.push(value);
            }else{//取消勾选
				object.attributeValue.splice( object.attributeValue.indexOf(value) ,1);//移除选项
                //如果选项都取消了，将此条记录移除
                if(object.attributeValue.length==0){
                    $scope.entity.tbGoodsDesc.specificationItems.splice(
                    $scope.entity.tbGoodsDesc.specificationItems.indexOf(object),1);

                }
            }
        }else{
            $scope.entity.tbGoodsDesc.specificationItems.push(
                {"attributeName":name,"attributeValue":[value]});
        }
        if($scope.entity.tbGoodsDesc.specificationItems.length==0){
            $scope.entity.tbItemList=[];
        }
    }

    //创建SKU列表
    $scope.createItemList=function(){
        $scope.entity.tbItemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' }];//初始
        var items=$scope.entity.tbGoodsDesc.specificationItems;
        for(var i=0;i< items.length;i++){
            $scope.entity.tbItemList = addColumn( $scope.entity.tbItemList,items[i].attributeName,items[i].attributeValue);
        }
    }
	//添加列值
    addColumn=function(list,columnName,conlumnValues){
        var newList=[];//新的集合
        for(var i=0;i<list.length;i++){
            var oldRow= list[i];
            for(var j=0;j<conlumnValues.length;j++){
                var newRow= JSON.parse(JSON.stringify(oldRow));//深克隆
                newRow.spec[columnName]=conlumnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }

    // 点击自定义规则,更改状态
    $scope.updateisEnableSpec=function($event){
        if(!$event.target.checked){
            $scope.entity.tbItemList=[];
            $scope.entity.tbGoodsDesc.specificationItems=[]
        }
    }




});	
