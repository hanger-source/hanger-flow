package script
import static source.hanger.flow.dsl.FlowDslEntry.*

// 这是一个复杂的业务流程，包含了所有定义的DSL元素
flow {
    name '订单处理与通知'
    description '处理客户订单，包括库存检查、支付、发货通知和并行处理'

    onEnter {
        println "[FLOW ON ENTER] 订单流程开始，初始化订单ID: ${context.orderId}"
        context.status = "初始化"
    }

    // 定义一个外部代理
    agent {
        name 'NotificationService'
        description '用于发送短信和邮件通知的外部服务'
        config {
            apiUrl "https://api.notify.com"
            apiKey "abc123xyz"
        }
    }

    task {
        name '库存检查'
        description '检查商品库存是否充足'
        run {
            println "任务 [库存检查] 执行中，订单ID: ${context.orderId}"
            context.itemCount = 10 // 模拟获取库存
            if (context.itemCount > 0) {
                println "库存充足。"
                context.stockOk = true
            } else {
                println "库存不足。"
                context.stockOk = false
            }
        }
        next { context.stockOk } to "支付处理"
        next { !context.stockOk } to "库存不足处理"
        onError {
            println "[ERROR] 库存检查失败: ${exception.message}"
            context.errorMessage = "库存检查异常"
        } to "通用错误处理"
    }

    task {
        name '支付处理'
        description '调用支付网关完成支付'
        run {
            println "任务 [支付处理] 执行中，订单ID: ${context.orderId}"
            // 模拟支付成功
            context.paid = true
        }
        next { context.paid } to "并行处理"
        next { !context.paid } to "支付失败处理"
    }

    entry {
        name '库存不足处理'
        description '当库存不足时进入此入口点'
        task {
            name "通知库存不足"
            description '通知用户库存不足，并终止订单'
            run {
                println "任务 [通知库存不足] 执行中，订单ID: ${context.orderId}"
                // 模拟调用 NotificationService.sendSms(context.userPhone, "库存不足")
                println "短信通知用户库存不足。"
                context.status = "库存不足"
            }
        }
    }

    entry {
        name '支付失败处理'
        description '支付失败时进入此入口点'
        task {
            name "通知支付失败"
            description '通知用户支付失败，并重置订单'
            run {
                println "任务 [通知支付失败] 执行中，订单ID: ${context.orderId}"
                println "短信通知用户支付失败。"
                context.status = "支付失败"
            }
        }
    }

    parallel {
        name '订单后续并行处理'
        description '支付成功后，同时进行发货准备和用户通知'

        branch {
            name "拣货打包"
            task {
                name "拣货打包"
                description '进行商品拣选和打包'
                run {
                    println "分支 [发货准备]：任务 [拣货打包] 执行中..."
                }
                next { true } to "物流分配"
            }
            task {
                name "物流分配"
                description '分配物流渠道'
                run {
                    println "分支 [发货准备]：任务 [物流分配] 执行中..."
                }
            }
        }

        branch {
            name "用户通知"
            task {
                name "发送订单确认邮件"
                description '通过邮件确认订单'
                run {
                    println "分支 [用户通知]：任务 [发送订单确认邮件] 执行中..."
                    // 模拟调用 NotificationService.sendEmail(...)
                }
            }
            task {
                name "发送短信通知"
                description '通过短信通知用户'
                run {
                    println "分支 [用户通知]：任务 [发送短信通知] 执行中..."
                    // 模拟调用 NotificationService.sendSms(...)
                }
            }
        }

        // 所有并行分支完成后，汇聚到“订单完成”任务
        next { true } to '订单完成'
    }

    task {
        name  '订单完成'
        description '流程结束，订单状态最终完成'
        run {
            println "任务 [订单完成] 执行中，订单ID: ${context.orderId}"
            context.status = "已完成"
            println "流程成功结束！"
        }
        nextTo END
    }

    entry {
        description '所有任务通用错误处理入口'
        task {
            name "记录错误日志"
            run {
                println "[ERROR GLOBAL] 捕获到通用错误，日志记录中。错误信息: ${context.errorMessage ?: '未知错误'}"
                // 实际的错误记录、报警逻辑
            }
            nextTo "通知管理员"
        }
        task {
            name "通知管理员"
            run {
                println "[ERROR GLOBAL] 通知管理员订单处理失败。"
                context.status = "处理失败"
            }
        }
    }
}