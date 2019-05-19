app.service('seckillGoodsService', function ($http) {
    this.findAll = function () {
        return $http.get('seckillGoods/findAll.do');
    }
    this.findOne = function (id) {
        return $http.get('seckillGoods/findOne.do?id=' + id);
    }
    this.submitOrder = function (seckillId) {
        return $http.get('seckillOrder/submitOrder.do?seckillId=' + seckillId);
    }
})