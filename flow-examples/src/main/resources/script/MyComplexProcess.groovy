package script

import static source.hanger.flow.dsl.FlowDslEntry.*

// 这是一个复杂的业务流程，包含了所有定义的DSL元素
flow {
    version '1.0.0'
    name '订单处理与通知'
    description """
    处理客户订单，包括库存检查、支付、发货准备和多种通知
    ...
    """

    onEnter {
        log "[FLOW ON ENTER] 订单流程开始，订单ID: ${context.params.orderId ?: 'N/A'}。初始化状态。"
        context.params.status = "初始化"
        // 模拟用户设置（用于条件分支判断）
        context.params.user = [prefersEmailNotification: true, hasPhoneNumber: true]
    }

    onError {
        // 这里的 onError 捕获未在 Task 级别处理的、导致流程中断的错误
        log "[FLOW ON ERROR] 订单处理全局失败，订单ID: ${context.params.orderId ?: 'N/A'}。错误: ${exception.message}。即将跳转到流程错误处理。"
        context.params.errorMessage = "全局流程错误: ${exception.message}" // 传递错误信息
    } to '流程错误处理'

    // 流程的起始点
    start '订单初始化'

    // --- Task 定义区域：所有的 Task 都是独立的，通过 nextTo 互相连接 ---
    task {
        name '订单初始化'
        description '准备订单数据，例如设置订单状态为待处理'
        run {
            log "任务 [订单初始化] 执行中，订单ID: ${context.params.orderId ?: 'N/A'}"
            context.params.status = "待处理"
            // 模拟获取订单商品列表，用于后续动态处理
            context.params.orderItems = [
                    [id: 'item001', type: 'electronics', qty: 1, price: 100],
                    [id: 'item002', type: 'book', qty: 2, price: 50]
            ]
        }
        nextTo '库存检查'
    }

    task {
        name '库存检查'
        description '检查所有订单商品的库存是否充足'

        onEnter {
            log "[TASK ON ENTER] 开始库存检查，订单ID: ${context.params.orderId ?: 'N/A'}"
        }

        run {
            log "任务 [库存检查] 执行中，订单ID: ${context.params.orderId ?: 'N/A'}"
            // 模拟遍历订单商品并检查库存
            boolean allStockOk = true
            context.params.orderItems.each { item ->
                // 模拟根据商品ID或类型检查库存
                if (item.id == 'item001' && item.qty > 0) { // 假设item001库存不足模拟
                    allStockOk = false
                    log "商品 ${item.id} 库存不足！"
                } else {
                    log "商品 ${item.id} 库存充足。"
                }
            }
            context.params.stockOk = allStockOk
        }

        // 根据库存检查结果进行条件跳转
        next { context.params.stockOk } to "支付处理"
        next { !context.params.stockOk } to "通知库存不足" // 库存不足直接通知并结束订单

        // Task 级别的错误处理，如果此任务失败（例如调用库存服务超时），会跳转到指定的错误处理任务
        onError {
            log "[ERROR] 库存检查任务失败: ${exception.message}"
            context.params.errorMessage = "库存检查异常: ${exception.message}"
        } to "记录错误日志" // 指向一个通用的错误日志记录任务
    }

    task {
        name '支付处理'
        description '调用支付网关完成支付'
        run {
            log "任务 [支付处理] 执行中，订单ID: ${context.params.orderId ?: 'N/A'}"
            // 模拟支付成功或失败
            // context.paid = true // 模拟成功
            context.params.paid = false // 模拟失败
            log "支付结果: ${context.params.paid ? '成功' : '失败'}"
        }
        // 根据支付结果进行条件跳转
        next { context.params.paid } to "支付成功并行处理" // 支付成功后，进入并行处理的起点
        next { !context.params.paid } to "通知支付失败" // 支付失败，通知用户并结束
    }

    task {
        name "通知库存不足"
        description '通知用户商品库存不足，并终止订单流程'
        run {
            log "任务 [通知库存不足] 执行中，订单ID: ${context.params.orderId ?: 'N/A'}"
            // 模拟调用 NotificationService.sendSms(context.userPhone, "很抱歉，您的订单商品库存不足。")
            log "短信通知用户：库存不足。"
            context.params.status = "库存不足，已关闭"
        }
        nextTo END // 此分支流程结束
    }

    task {
        name "通知支付失败"
        description '通知用户支付失败，并引导重试或取消订单'
        run {
            log "任务 [通知支付失败] 执行中，订单ID: ${context.params.orderId ?: 'N/A'}"
            log "邮件通知用户：支付失败，请尝试重新支付或联系客服。"
            context.params.status = "支付失败，待重试"
        }
        nextTo END // 此分支流程结束
    }

    async { // 给这个异步流命名，方便追踪
        name '发送短信通知异步流'
        branch '发送短信通知' // 异步触发这个任务
    }

    // --- 支付成功后的并行处理块定义 ---
    // parallel 块本身是一个流程点，它内部定义了多个并发分支的起点
    // 并且汇合点由 parallel 块的 nextTo 定义
    parallel {
        name '支付成功并行处理'
        description '支付成功后，同时进行发货准备和多种用户通知'

        // 无条件启动的并行分支
        branch '物流分配'
        branch '拣货打包'

        // 带有条件的并行分支启动
        // `when { condition } to '任务名称'` 语法
        branch '发送订单确认邮件' when { context.params.user.prefersEmailNotification } // 如果用户偏好邮件，则发送确认邮件

        // 异步发送短信通知，可以独立于主要并行分支，不阻碍主汇合
        branch '发送短信通知异步流'
        // 汇合点：等待 '物流分配' 和 '拣货打包' 这两个核心分支完成
        // 其他条件启动的或async启动的分支不影响此处的汇合
        waitFor '物流分配', '拣货打包' nextTo '订单完成'
    }

    // --- 并行分支内的 Task 定义 ---
    // 这些 Task 都是独立的，它们的 nextTo 默认会到 END，或根据后续业务逻辑指向其他任务
    // 但它们的完成状态会被 `parallel` 块或 `async` 块感知。

    task {
        name "物流分配" // 作为并行分支的起点
        description '为订单分配物流渠道，生成物流单号'
        run {
            log "并行分支 [物流分配]：任务 [物流分配] 执行中...分配单号: L${context.params.orderId}"
            context.params.logisticsAssigned = true
        }
    }

    task {
        name "拣货打包" // 作为并行分支的起点
        description '根据订单商品进行拣选和打包，更新库存'
        run {
            log "并行分支 [拣货打包]：任务 [拣货打包] 执行中..."
            context.params.itemsPacked = true
        }
    }

    task {
        name "发送订单确认邮件" // 作为条件启动的并行分支的起点
        description '通过邮件向用户发送订单确认信息'
        run {
            log "条件并行分支 [发送订单确认邮件]：任务 [发送订单确认邮件] 执行中..."
            // 模拟调用 NotificationService.sendEmail(context.userEmail, "您的订单已确认")
        }
    }

    task {
        name "发送短信通知" // 作为完全异步流的起点
        description '通过短信通知用户订单支付成功'
        run {
            log "异步通知流 [发送短信通知]：任务 [发送短信通知] 执行中..."
            // 模拟调用 NotificationService.sendSms(context.userPhone, "您的订单已支付成功")
        }
    }

    task {
        name  '订单完成'
        description '流程成功结束，订单状态最终完成并更新到数据库'
        run {
            log "任务 [订单完成] 执行中，订单ID: ${context.params.orderId ?: 'N/A'}"
            context.params.status = "已完成"
            log "流程成功结束！"
        }
        nextTo END // 明确指示主流程在此处结束
    }

    // --- 错误处理 Task 定义 (集中处理流程中捕获的错误) ---
    task {
        name "流程错误处理" // 作为 onError 的目标
        description "捕获全局或未处理的流程错误，并进行统一处理"
        run {
            log "[ERROR GLOBAL] 捕获到流程级别错误，执行通用错误处理。错误信息: ${context.params.errorMessage ?: '未知错误'}"
            // 这里可以进行：回滚操作、告警通知、记录详细日志等
            context.params.status = "流程异常终止"
        }
        nextTo "通知管理员" // 错误处理完后，继续通知管理员
    }

    task {
        name "记录错误日志" // 作为 Task 级别 onError 的目标
        description "记录任务级别的错误日志，通常比全局错误更细致"
        run {
            log "[ERROR TASK] 捕获到任务级别错误，日志记录中。错误信息: ${context.params.errorMessage ?: '未知错误'}"
            // 记录到具体的错误日志系统
            context.params.status = "任务失败"
        }
        nextTo "通知管理员" // 记录完日志后，继续通知管理员
    }

    task {
        name "通知管理员"
        description "通知相关管理员流程实例失败或出现异常"
        run {
            log "[ERROR NOTIFICATION] 通知管理员订单处理失败，订单ID: ${context.params.orderId ?: 'N/A'}。"
            // 模拟发送邮件或触发告警系统
        }
    }
}