package com.shopjoy.aspect;

import com.shopjoy.util.AspectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
public class TransactionAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionAspect.class);
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final long LONG_TRANSACTION_THRESHOLD = 5000;
    
    @Before("@within(org.springframework.transaction.annotation.Transactional)")
    public void logTransactionalClassStart(JoinPoint joinPoint) {
        logTransactionStart(joinPoint);
    }
    
    @Before("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void logTransactionStart(JoinPoint joinPoint) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String methodSignature = AspectUtils.extractMethodSignature(joinPoint);
        
        logger.info("[{}] TRANSACTION START: {}", timestamp, methodSignature);
    }
    
    @AfterReturning("@within(org.springframework.transaction.annotation.Transactional)")
    public void logTransactionalClassCommit(JoinPoint joinPoint) {
        logTransactionCommit(joinPoint);
    }
    
    @AfterReturning("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void logTransactionCommit(JoinPoint joinPoint) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String methodSignature = AspectUtils.extractMethodSignature(joinPoint);
        
        logger.info("[{}] TRANSACTION COMMIT: {}", timestamp, methodSignature);
    }
    
    @AfterThrowing(pointcut = "@within(org.springframework.transaction.annotation.Transactional)", throwing = "exception")
    public void logTransactionalClassRollback(JoinPoint joinPoint, Exception exception) {
        logTransactionRollback(joinPoint, exception);
    }
    
    @AfterThrowing(pointcut = "@annotation(org.springframework.transaction.annotation.Transactional)", throwing = "exception")
    public void logTransactionRollback(JoinPoint joinPoint, Exception exception) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String methodSignature = AspectUtils.extractMethodSignature(joinPoint);
        
        logger.error("[{}] TRANSACTION ROLLBACK: {} - Exception: {}", 
            timestamp, methodSignature, exception.getMessage());
    }
    
    @Around("@within(org.springframework.transaction.annotation.Transactional) || @annotation(org.springframework.transaction.annotation.Transactional)")
    public Object monitorTransactionDuration(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodSignature = AspectUtils.extractMethodSignature(joinPoint);
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            if (duration > LONG_TRANSACTION_THRESHOLD) {
                logger.warn("LONG RUNNING TRANSACTION: {} took {}", 
                    methodSignature, AspectUtils.formatExecutionTime(duration));
            } else {
                logger.debug("Transaction {} completed in {}", 
                    methodSignature, AspectUtils.formatExecutionTime(duration));
            }
            
            return result;
        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Transaction {} failed after {} with exception: {}", 
                methodSignature, AspectUtils.formatExecutionTime(duration), t.getMessage());
            throw t;
        }
    }
    
    @Before("com.shopjoy.aspect.CommonPointcuts.dataModificationMethods() && @annotation(org.springframework.transaction.annotation.Transactional)")
    public void logDataModificationTransaction(JoinPoint joinPoint) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String methodSignature = AspectUtils.extractMethodSignature(joinPoint);
        String args = AspectUtils.sanitizeArgs(joinPoint.getArgs());
        
        logger.info("[{}] TRANSACTIONAL DATA MODIFICATION: {} with arguments: {}", 
            timestamp, methodSignature, args);
    }
}
