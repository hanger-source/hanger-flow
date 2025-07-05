package script

import static source.hanger.flow.dsl.FlowDslEntry.*

flow {
    version '2.0.0'
    name '超级复杂订单处理'
    description '''
    演示多层嵌套、动态分支、循环重试、聚合、全链路参数流转、全局与局部onError等极致复杂流程
    '''

    onEnter {
        log "[FLOW ON ENTER] 超级复杂流程启动，订单ID: ${context.params.orderId ?: 'N/A'}"
        context.params.status = "初始化"
        context.params.retryCount = 0
        context.params.items = (1..4).collect { [id: "item$it", qty: it, price: it*10] }
    }

    onError {
        log "[FLOW ON ERROR] 全局异常: ${exception.message}"
        context.params.errorMessage = "全局异常: ${exception.message}"
    } to '全局错误处理'

    start '订单初始化'

    task {
        name '订单初始化'
        description '初始化订单参数'
        run {
            log "初始化订单，商品数: ${context.params.items.size()}"
            context.params.status = "待处理"
        }
        nextTo '多商品并行处理'
    }

    parallel {
        name '多商品并行处理'
        description '每个商品一个分支，分支内还可并行和异步'
        branch '商品1处理'
        branch '商品2处理'
        branch '商品3处理'
        branch '商品4处理'
        waitFor '商品1处理', '商品2处理', '商品3处理', '商品4处理' nextTo '聚合处理结果'
    }

    // 商品分支任务，分支内再嵌套并行和异步
    (1..4).each { idx ->
        parallel {
            name "商品${idx}处理"
            description "商品${idx}分支内并行和异步"
            branch "商品${idx}-库存检查"
            branch "商品${idx}-价格校验"
            branch "商品${idx}-异步通知流"
            waitFor "商品${idx}-库存检查", "商品${idx}-价格校验" nextTo "商品${idx}-分支聚合"
        }
        task {
            name "商品${idx}-库存检查"
            run {
                log "[${idx}] 库存检查"
                context.params["item${idx}_stockOk"] = idx % 2 == 0
            }
            nextTo "商品${idx}-库存结果处理"
        }
        task {
            name "商品${idx}-价格校验"
            run {
                log "[${idx}] 价格校验"
                context.params["item${idx}_priceOk"] = idx % 2 == 1
            }
            nextTo "商品${idx}-价格结果处理"
        }
        async {
            name "商品${idx}-异步通知流"
            branch "商品${idx}-异步通知"
        }
        task {
            name "商品${idx}-异步通知"
            run {
                log "[${idx}] 异步通知：商品处理进度提醒"
            }
        }
        task {
            name "商品${idx}-库存结果处理"
            run {
                log "[${idx}] 库存结果处理: ${context.params["item${idx}_stockOk"]}"
            }
            nextTo "商品${idx}-分支聚合"
        }
        task {
            name "商品${idx}-价格结果处理"
            run {
                log "[${idx}] 价格结果处理: ${context.params["item${idx}_priceOk"]}"
            }
            nextTo "商品${idx}-分支聚合"
        }
        task {
            name "商品${idx}-分支聚合"
            run {
                log "[${idx}] 分支聚合"
            }
        }
    }

    task {
        name '聚合处理结果'
        run {
            log "聚合所有商品处理结果"
            context.params.allStockOk = (1..4).every { context.params["item${it}_stockOk"] }
            context.params.allPriceOk = (1..4).every { context.params["item${it}_priceOk"] }
        }
        next { context.params.allStockOk && context.params.allPriceOk } to '支付处理'
        next { !context.params.allStockOk || !context.params.allPriceOk } to '循环重试'
    }

    task {
        name '循环重试'
        run {
            context.params.retryCount = (context.params.retryCount ?: 0) + 1
            log "聚合校验未通过，重试第${context.params.retryCount}次"
        }
        next { context.params.retryCount < 2 } to '多商品并行处理'
        next { context.params.retryCount >= 2 } to '终止流程'
    }

    task {
        name '支付处理'
        run {
            log "支付处理"
            context.params.paid = true
        }
        nextTo '订单完成'
    }

    task {
        name '终止流程'
        run {
            log "终止流程，订单失败"
            context.params.status = "终止"
        }
        nextTo END
    }

    task {
        name '订单完成'
        run {
            log "订单完成，流程成功结束"
            context.params.status = "已完成"
        }
        nextTo END
    }

    task {
        name '全局错误处理'
        run {
            log "全局错误处理: ${context.params.errorMessage ?: '未知错误'}"
            context.params.status = "全局异常终止"
        }
        nextTo END
    }
} 