package source.hanger.flow.dsl.hint

import groovy.transform.CompileStatic

/**
 * @author fuhangbo.hanger.uhfun
 * */
@CompileStatic
trait AsyncHint {

    abstract void name(String text)

    abstract void description(String text)

    abstract void branch(String text)
}