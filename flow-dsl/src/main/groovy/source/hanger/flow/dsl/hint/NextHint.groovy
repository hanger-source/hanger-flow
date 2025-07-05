package source.hanger.flow.dsl.hint

import groovy.transform.CompileStatic

/**
 * @author fuhangbo.hanger.uhfun 
 * */
@CompileStatic
trait NextHint {
    abstract void to(String nextStepName)
}