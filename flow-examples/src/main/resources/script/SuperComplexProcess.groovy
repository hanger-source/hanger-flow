package script

import static source.hanger.flow.dsl.FlowDslEntry.*

flow {
    version '2.0.0'
    name '超级复杂订单处理'
    description '''
    演示多层嵌套、动态分支、循环重试、聚合、全链路参数流转、全局与局部onError等极致复杂流程
    '''

    onEnter {
        log "[FLOW ON ENTER] 超级复杂流程启动，订单ID: ${inputs.orderId ?: 'N/A'}"
        context.status = "初始化"
        context.retryCount = 0
        context.items = (1..4).collect { [id: "item$it", qty: it, price: it * 10] }
    }

    onError {
        log "[FLOW ON ERROR] 全局异常: ${error.message}"
        context.errorMessage = "全局异常: ${error.message}"
    } to '全局错误处理'

    start '订单初始化'

    task {
        name '订单初始化'
        description '初始化订单参数'
        run {
            log "初始化订单，商品数: ${inputs.items.size()}"
            context.status = "待处理"
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

    // 商品1分支
    parallel {
        name "商品1处理"
        description "商品1分支内并行和异步"
        branch "商品1-库存检查"
        branch "商品1-价格校验"
        branch "商品1-异步通知流"
        waitFor "商品1-库存检查", "商品1-价格校验" nextTo "商品1-分支聚合"
    }
    task {
        name "商品1-库存检查"
        run {
            log "[1] 库存检查"
            context["item1_stockOk"] = Math.random() > 0.5
        }
        nextTo "商品1-库存结果处理"
    }
    task {
        name "商品1-价格校验"
        run {
            log "[1] 价格校验"
            context["item1_priceOk"] = Math.random() > 0.5
        }
        nextTo "商品1-价格结果处理"
    }
    async {
        name "商品1-异步通知流"
        branch "商品1-异步通知"
    }
    task {
        name "商品1-异步通知"
        run {
            log "[1] 异步通知：商品处理进度提醒"
        }
    }
    task {
        name "商品1-库存结果处理"
        run {
            log "[1] 库存结果处理: ${context["item1_stockOk"]}"
        }
        nextTo "商品1-分支聚合"
    }
    task {
        name "商品1-价格结果处理"
        run {
            log "[1] 价格结果处理: ${context["item1_priceOk"]}"
        }
        nextTo "商品1-分支聚合"
    }
    task {
        name "商品1-分支聚合"
        run {
            log "[1] 分支聚合"
        }
    }
    // 商品2分支
    parallel {
        name "商品2处理"
        description "商品2分支内并行和异步"
        branch "商品2-库存检查"
        branch "商品2-价格校验"
        branch "商品2-异步通知流"
        waitFor "商品2-库存检查", "商品2-价格校验" nextTo "商品2-分支聚合"
    }
    task {
        name "商品2-库存检查"
        run {
            log "[2] 库存检查"
            context["item2_stockOk"] = Math.random() > 0.5
        }
        nextTo "商品2-库存结果处理"
    }
    task {
        name "商品2-价格校验"
        run {
            log "[2] 价格校验"
            context["item2_priceOk"] = Math.random() > 0.5
        }
        nextTo "商品2-价格结果处理"
    }
    async {
        name "商品2-异步通知流"
        branch "商品2-异步通知"
    }
    task {
        name "商品2-异步通知"
        run {
            log "[2] 异步通知：商品处理进度提醒"
        }
    }
    task {
        name "商品2-库存结果处理"
        run {
            log "[2] 库存结果处理: ${context["item2_stockOk"]}"
        }
        nextTo "商品2-分支聚合"
    }
    task {
        name "商品2-价格结果处理"
        run {
            log "[2] 价格结果处理: ${context["item2_priceOk"]}"
        }
        nextTo "商品2-分支聚合"
    }
    task {
        name "商品2-分支聚合"
        run {
            log "[2] 分支聚合"
        }
    }
    // 商品3分支
    parallel {
        name "商品3处理"
        description "商品3分支内并行和异步"
        branch "商品3-库存检查"
        branch "商品3-价格校验"
        branch "商品3-异步通知流"
        waitFor "商品3-库存检查", "商品3-价格校验" nextTo "商品3-分支聚合"
    }
    task {
        name "商品3-库存检查"
        run {
            log "[3] 库存检查"
            context["item3_stockOk"] = Math.random() > 0.5
        }
        nextTo "商品3-库存结果处理"
    }
    task {
        name "商品3-价格校验"
        run {
            log "[3] 价格校验"
            context["item3_priceOk"] = Math.random() > 0.5
        }
        nextTo "商品3-价格结果处理"
    }
    async {
        name "商品3-异步通知流"
        branch "商品3-异步通知"
    }
    task {
        name "商品3-异步通知"
        run {
            log "[3] 异步通知：商品处理进度提醒"
        }
    }
    task {
        name "商品3-库存结果处理"
        run {
            log "[3] 库存结果处理: ${context["item3_stockOk"]}"
        }
        nextTo "商品3-分支聚合"
    }
    task {
        name "商品3-价格结果处理"
        run {
            log "[3] 价格结果处理: ${context["item3_priceOk"]}"
        }
        nextTo "商品3-分支聚合"
    }
    task {
        name "商品3-分支聚合"
        run {
            log "[3] 分支聚合"
        }
    }
    // 商品4分支
    parallel {
        name "商品4处理"
        description "商品4分支内并行和异步"
        branch "商品4-库存检查"
        branch "商品4-价格校验"
        branch "商品4-异步通知流"
        waitFor "商品4-库存检查", "商品4-价格校验" nextTo "商品4-分支聚合"
    }
    task {
        name "商品4-库存检查"
        run {
            log "[4] 库存检查"
            context["item4_stockOk"] = Math.random() > 0.5
        }
        nextTo "商品4-库存结果处理"
    }
    task {
        name "商品4-价格校验"
        run {
            log "[4] 价格校验"
            context["item4_priceOk"] = Math.random() > 0.5
        }
        nextTo "商品4-价格结果处理"
    }
    async {
        name "商品4-异步通知流"
        branch "商品4-异步通知"
    }
    task {
        name "商品4-异步通知"
        run {
            log "[4] 异步通知：商品处理进度提醒"
        }
    }
    task {
        name "商品4-库存结果处理"
        run {
            log "[4] 库存结果处理: ${context["item4_stockOk"]}"
        }
        nextTo "商品4-分支聚合"
    }
    task {
        name "商品4-价格结果处理"
        run {
            log "[4] 价格结果处理: ${context["item4_priceOk"]}"
        }
        nextTo "商品4-分支聚合"
    }
    task {
        name "商品4-分支聚合"
        run {
            log "[4] 分支聚合"
        }
    }

    task {
        name '聚合处理结果'
        run {
            log "聚合所有商品处理结果"
            context.allStockOk = (1..4).every { context["item${it}_stockOk"] }
            context.allPriceOk = (1..4).every { context["item${it}_priceOk"] }
        }
        next {
            context.allStockOk && context.allPriceOk
        } to '支付处理'
        next {
            !context.allStockOk || !context.allPriceOk
        } to '循环重试'
    }

    task {
        name '循环重试'
        run {
            context.retryCount = (context.retryCount ?: 0) + 1
            log "聚合校验未通过，重试第${context.retryCount}次"
        }
        next {
            context.retryCount < 2
        } to '多商品并行处理'
        next {
            context.retryCount >= 2
        } to '终止流程'
    }

    task {
        name '支付处理'
        run {
            log "支付处理"
            context.paid = true
        }
        nextTo '订单完成'
    }

    task {
        name '终止流程'
        run {
            log "终止流程，订单失败"
            context.status = "终止"
        }
        nextTo END
    }

    task {
        name '订单完成'
        run {
            log "订单完成，流程成功结束"
            context.status = "已完成"
        }
        nextTo END
    }

    task {
        name '全局错误处理'
        run {
            log "全局错误处理: ${context.errorMessage ?: '未知错误'}"
            context.status = "全局异常终止"
        }
        nextTo END
    }
} 