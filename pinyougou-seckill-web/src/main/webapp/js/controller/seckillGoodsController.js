app.controller('seckillGoodsController', function ($scope, $location, seckillGoodsService, $interval) {
    $scope.findAll = function () {
        seckillGoodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        )
    }

    $scope.findOne = function () {
        var id = $location.search()['id'];
        seckillGoodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                allsecond = Math.floor(new Date($scope.entity.endTime).getTime() / 1000 - new Date().getTime() / 1000);

                time = $interval(function () {
                    $scope.timeString = convertTimeString(allsecond);
                    if (allsecond > 0) {
                        allsecond = allsecond - 1;
                    } else {
                        $interval.cancel(time);
                        alert("秒杀服务已结束");
                    }
                }, 1000);
            }
        )
    }
    //转换秒为   天小时分钟秒格式  XXX天 10:22:33
    convertTimeString = function (allsecond) {
        var days = Math.floor(allsecond / (60 * 60 * 24));//天数
        var hours = Math.floor((allsecond - days * 60 * 60 * 24) / (60 * 60));//小数数
        var minutes = Math.floor((allsecond - days * 60 * 60 * 24 - hours * 60 * 60) / 60);//分钟数
        var seconds = allsecond - days * 60 * 60 * 24 - hours * 60 * 60 - minutes * 60; //秒数
        if (days > 0) {
            days = days + "天 ";
        }
        if (hours < 10) {
            hours = "0" + hours;
        }
        if (minutes < 10) {
            minutes = "0" + minutes;
        }
        if (seconds < 10) {
            seconds = "0" + seconds;
        }
        return days + hours + ":" + minutes + ":" + seconds;
    }
    $scope.submitOrder = function () {
        seckillGoodsService.submitOrder($scope.entity.id).success(
            function (response) {
                if (response.success) {
                    //成功不代表下单成功，只能表示排队成功 正在做订单处理
                    //定时 每三秒钟查询一次 查询一分钟
                    $scope.timestatus = 20;
                    time = $interval(function () {
                        if (allsecond > 0) {
                            $scope.timestatus = $scope.timestatus - 1;
                            seckillGoodsService.queryOrderStatus().success(
                                function (response) {//
                                    if (response.success) {
                                        //跳转到支付页面
                                        window.location.href = "pay.html";
                                    } else if (response.message == '402') {
                                        alert("正在排队中,请稍等");
                                    } else if (response.message = '404') {
                                        alert("下单失败");
                                        //停止查询
                                        $interval.cancel(time);
                                    }
                                }
                            )

                        } else {
                            //查询60秒结束
                            $interval.cancel(time);
                        }
                    }, 3000);
                } else {
                    //失败 有可能是因为没有登录 和其他的原因  我们要分开来做
                    if (response.message == '401') {
                        //未登录
                        var url = window.location.href;
                        window.location.href = "/page/login.do?url=" + encodeURIComponent(url);
                    } else {
                        alert(response.message);
                    }

                }
            }
        );
    }

    $scope.queryPayStatus = function () {
        //每3秒钟查询一次，总共查询5分钟
        $scope.seconds = 100;

        time = $interval(function () {
            if ($scope.seconds > 0) {
                $scope.seconds = $scope.seconds - 1;

                //订单状态查询
                payService.queryPayStatus($scope.out_trade_no).success(function (response) {
                    if (response.success) {
                        //支付成功   跳转到支付成功页面
                        location.href = '/paysuccess.html#?money=' + $scope.total_fee;
                    } else {
                        if (response.message == '404') {
                            //支付失败
                            location.href = '/payfail.html';
                        } else {
                            alert(response.message);
                            //$interval.cancel(time);
                        }
                    }
                })

            } else {
                $interval.cancel(time);
                alert("支付时间超时！")
            }
        }, 3000);
    }

})