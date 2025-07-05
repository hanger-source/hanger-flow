package source.hanger.flow.dsl.hint

import groovy.transform.CompileStatic

/**
 * @author fuhangbo.hanger.uhfun 
 * */
@CompileStatic
trait JoinHint {
    abstract void nextTo(String nextStepName)
}