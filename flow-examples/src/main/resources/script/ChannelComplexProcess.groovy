package script

import static source.hanger.flow.dsl.FlowDslEntry.*

// 这是一个使用channel buffer通信的复杂业务流程示例
flow {
    version '1.0.0'
    name 'Channel通信订单处理流程'
    description """
    演示使用channel buffer进行步骤间通信的复杂流程
    包括数据推送、监听、等待完整数据等功能
    """

    onEnter {
        log "[FLOW ON ENTER] Channel通信流程开始，订单ID: ${inputs.orderId ?: 'N/A'}"
        inputs.status = "初始化"
        inputs.channelData = [:]
    }

    onError {
        log "[FLOW ON ERROR] Channel通信流程失败，订单ID: ${inputs.orderId ?: 'N/A'}。错误: ${error.message}"
        context.errorMessage = "Channel流程错误: ${error.message}"
    } to '流程错误处理'

    start '数据收集'

    // 数据收集步骤 - 推送数据到channel
    task {
        name '数据收集'
        description '收集订单基础数据并推送到channel'
        run {
            log "任务 [数据收集] 执行中，订单ID: ${inputs.orderId}"
            
            // 模拟收集订单数据
            def orderData = [
                orderId: inputs.orderId,
                customerId: "CUST_${System.currentTimeMillis()}",
                items: [
                    [id: 'item001', name: 'iPhone 15', qty: 1, price: 5999],
                    [id: 'item002', name: 'AirPods Pro', qty: 1, price: 1899]
                ],
                totalAmount: 7898,
                createTime: System.currentTimeMillis()
            ]
            
            // 推送订单数据到channel
            channel.acquireBuffer('order_data').pushFragment(orderData)
            log "已推送订单数据到channel: order_data"
            
            // 推送客户信息
            def customerData = [
                customerId: orderData.customerId,
                name: "张三",
                email: "zhangsan@example.com",
                phone: "13800138000",
                address: "北京市朝阳区xxx街道"
            ]
            channel.acquireBuffer('customer_data').pushFragment(customerData)
            log "已推送客户数据到channel: customer_data"
            
            // 标记数据收集完成
            channel.acquireBuffer('order_data').pushDone()
            channel.acquireBuffer('customer_data').pushDone()
            
            inputs.orderData = orderData
            inputs.customerData = customerData
        }
        nextTo '库存检查'
    }

    // 库存检查步骤 - 监听订单数据并推送库存信息
    task {
        name '库存检查'
        description '监听订单数据并检查库存，推送库存状态'
        
        onEnter {
            log "[TASK ON ENTER] 开始库存检查，等待订单数据..."
        }
        
        run {
            log "任务 [库存检查] 执行中，订单ID: ${inputs.orderId}"
            // 等待并接收订单数据
            channel.getBuffer('order_data').onReceive { chunk ->
                if (chunk.isFragment()) {
                    def orderData = chunk.data
                    log "接收到订单数据: ${orderData}"
                    // 模拟库存检查
                    def stockResults = [:]
                    orderData.items.each { item ->
                        def stockInfo = [
                            itemId: item.id,
                            itemName: item.name,
                            requiredQty: item.qty,
                            availableQty: item.id == 'item001' ? 0 : 10, // item001库存不足
                            inStock: item.id == 'item001' ? false : true
                        ]
                        stockResults[item.id] = stockInfo
                        log "商品 ${item.name} 库存检查: ${stockInfo.inStock ? '充足' : '不足'}"
                    }
                    // 推送库存检查结果
                    channel.acquireBuffer('stock_check_result').pushFragment(stockResults)
                    channel.acquireBuffer('stock_check_result').pushDone()
                    // 判断整体库存状态
                    def allInStock = stockResults.values().every { it.inStock }
                    inputs.stockOk = allInStock
                    inputs.stockResults = stockResults
                    log "库存检查完成，整体状态: ${allInStock ? '充足' : '不足'}"
                }
            }
        }
        
        next { inputs.stockOk } to "支付处理"
        next { !inputs.stockOk } to "库存不足处理"
        
        onError {
            log "[ERROR] 库存检查任务失败: ${error.message}"
            context.errorMessage = "库存检查异常: ${error.message}"
        } to "记录错误日志"
    }

    // 库存不足处理 - 监听库存数据并处理
    task {
        name '库存不足处理'
        description '处理库存不足情况，通知客户'
        run {
            log "任务 [库存不足处理] 执行中"
            channel.getBuffer('stock_check_result').onReceive { chunk ->
                if (chunk.isFragment()) {
                    def stockResults = chunk.data
                    log "接收到库存检查结果: ${stockResults}"
                    def outOfStockItems = stockResults.values().findAll { !it.inStock }
                    def notificationData = [
                        type: 'stock_shortage',
                        items: outOfStockItems,
                        message: "很抱歉，以下商品库存不足: ${outOfStockItems.collect { it.itemName }.join(', ')}"
                    ]
                    channel.acquireBuffer('notification_data').pushFragment(notificationData)
                    channel.acquireBuffer('notification_data').pushDone()
                    inputs.status = "库存不足，已关闭"
                    log "库存不足处理完成，已推送通知数据"
                }
            }
        }
        nextTo END
    }

    // 支付处理 - 监听订单和客户数据
    task {
        name '支付处理'
        description '监听订单和客户数据，进行支付处理'
        run {
            log "任务 [支付处理] 执行中"
            channel.getBuffer('order_data').onReceive { chunk ->
                if (chunk.isFragment()) {
                    def orderData = chunk.data
                    log "接收到订单数据: ${orderData.orderId}"
                    channel.getBuffer('customer_data').onReceive { c2 ->
                        if (c2.isFragment()) {
                            def customerData = c2.data
                            log "接收到客户数据: ${customerData.name}"
                            def paymentResult = [
                                orderId: orderData.orderId,
                                customerId: customerData.customerId,
                                amount: orderData.totalAmount,
                                paymentMethod: "信用卡",
                                paymentStatus: Math.random() > 0.3,
                                transactionId: "TXN_${System.currentTimeMillis()}",
                                paymentTime: System.currentTimeMillis()
                            ]
                            channel.acquireBuffer('payment_result').pushFragment(paymentResult)
                            channel.acquireBuffer('payment_result').pushDone()
                            inputs.paymentResult = paymentResult
                            inputs.paid = paymentResult.paymentStatus
                            log "支付处理完成，状态: ${paymentResult.paymentStatus ? '成功' : '失败'}"
                        }
                    }
                }
            }
        }
        
        next { inputs.paid } to "支付成功并行处理"
        next { !inputs.paid } to "支付失败处理"
    }

    // 支付失败处理
    task {
        name '支付失败处理'
        description '处理支付失败情况'
        run {
            log "任务 [支付失败处理] 执行中"
            channel.getBuffer('payment_result').onReceive { chunk ->
                if (chunk.isFragment()) {
                    def paymentResult = chunk.data
                    // 推送支付失败通知
                    def notificationData = [
                        type: 'payment_failed',
                        paymentResult: paymentResult,
                        message: "支付失败，订单ID: ${paymentResult.orderId}"
                    ]
                    channel.acquireBuffer('notification_data').pushFragment(notificationData)
                    channel.acquireBuffer('notification_data').pushDone()
                    log "支付失败处理完成，已推送通知数据"
                }
            }
        }
        nextTo END
    }

    // 支付成功后的并行处理
    parallel {
        name '支付成功并行处理'
        description '支付成功后，并行处理物流、库存更新和通知'
        
        branch '物流处理'
        branch '库存更新'
        branch '通知处理'
        
        waitFor '物流处理', '库存更新', '通知处理' nextTo '订单完成'
    }

    // 物流处理 - 监听支付数据
    task {
        name '物流处理'
        description '监听支付数据，进行物流分配'
        run {
            log "并行分支 [物流处理] 执行中"
            
            // 等待支付结果
            def paymentResult = channel.receive('payment_result')
            log "接收到支付结果，开始物流处理: ${paymentResult.orderId}"
            
            // 模拟物流分配
            def logisticsData = [
                orderId: paymentResult.orderId,
                logisticsCompany: "顺丰速运",
                trackingNumber: "SF${System.currentTimeMillis()}",
                estimatedDelivery: System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000, // 3天后
                status: "已分配"
            ]
            
            // 推送物流数据
            channel.emit('logistics_data', logisticsData)
            channel.done('logistics_data')
            
            inputs.logisticsData = logisticsData
            log "物流处理完成: ${logisticsData.trackingNumber}"
        }
    }

    // 库存更新 - 监听订单和支付数据
    task {
        name '库存更新'
        description '监听订单和支付数据，更新库存'
        run {
            log "并行分支 [库存更新] 执行中"
            
            // 等待订单和支付数据
            def orderData = channel.receive('order_data')
            def paymentResult = channel.receive('payment_result')
            log "接收到订单和支付数据，开始库存更新"
            
            // 模拟库存更新
            def inventoryUpdate = [
                orderId: orderData.orderId,
                items: orderData.items.collect { item ->
                    [
                        itemId: item.id,
                        itemName: item.name,
                        originalStock: 10,
                        soldQty: item.qty,
                        remainingStock: 10 - item.qty,
                        updateTime: System.currentTimeMillis()
                    ]
                },
                status: "已更新"
            ]
            
            // 推送库存更新数据
            channel.emit('inventory_update', inventoryUpdate)
            channel.done('inventory_update')
            
            inputs.inventoryUpdate = inventoryUpdate
            log "库存更新完成"
        }
    }

    // 通知处理 - 监听各种数据并发送通知
    task {
        name '通知处理'
        description '监听各种数据并发送相应的通知'
        run {
            log "并行分支 [通知处理] 执行中"
            
            // 等待各种数据
            def orderData = channel.receive('order_data')
            def customerData = channel.receive('customer_data')
            def paymentResult = channel.receive('payment_result')
            
            log "接收到所有必要数据，开始发送通知"
            
            // 发送订单确认通知
            def confirmationNotification = [
                type: 'order_confirmation',
                customerId: customerData.customerId,
                customerName: customerData.name,
                customerEmail: customerData.email,
                orderId: orderData.orderId,
                amount: paymentResult.amount,
                message: "您的订单已确认，感谢您的购买！"
            ]
            channel.emit('notification_data', confirmationNotification)
            
            // 发送物流通知
            def logisticsNotification = [
                type: 'logistics_notification',
                customerId: customerData.customerId,
                customerPhone: customerData.phone,
                orderId: orderData.orderId,
                message: "您的订单已发货，物流单号将稍后发送"
            ]
            channel.emit('notification_data', logisticsNotification)
            
            channel.done('notification_data')
            
            log "通知处理完成，已发送确认和物流通知"
        }
    }

    // 订单完成 - 等待所有并行分支完成
    task {
        name '订单完成'
        description '等待所有并行分支完成，汇总最终结果'
        run {
            log "任务 [订单完成] 执行中"
            
            // 等待所有相关数据
            def orderData = channel.receive('order_data')
            def customerData = channel.receive('customer_data')
            def paymentResult = channel.receive('payment_result')
            def logisticsData = channel.receive('logistics_data')
            def inventoryUpdate = channel.receive('inventory_update')
            
            log "接收到所有数据，汇总订单完成信息"
            
            // 创建最终订单汇总
            def orderSummary = [
                orderId: orderData.orderId,
                customerInfo: customerData,
                paymentInfo: paymentResult,
                logisticsInfo: logisticsData,
                inventoryInfo: inventoryUpdate,
                status: "已完成",
                completeTime: System.currentTimeMillis()
            ]
            
            // 推送最终汇总数据
            channel.emit('order_summary', orderSummary)
            channel.done('order_summary')
            
            inputs.orderSummary = orderSummary
            inputs.status = "已完成"
            
            log "订单处理流程完成！订单ID: ${orderData.orderId}"
        }
        nextTo END
    }

    // 错误处理任务
    task {
        name '流程错误处理'
        description '处理流程级别的错误'
        run {
            log "[ERROR GLOBAL] 捕获到流程级别错误: ${context.errorMessage}"
            inputs.status = "流程异常终止"
        }
        nextTo '记录错误日志'
    }

    task {
        name '记录错误日志'
        description '记录错误日志'
        run {
            log "[ERROR TASK] 记录错误日志: ${context.errorMessage}"
            inputs.status = "任务失败"
        }
        nextTo END
    }
} 