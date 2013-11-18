package org.kevoree.library.defaultNodeTypes.wrapper

import org.kevoree.framework.KInstance
import org.kevoree.ContainerRoot

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 17/11/2013
 * Time: 20:03
 */

public class KevoreeNodeWrapper() : KInstance {

    override fun kInstanceStart(tmodel: ContainerRoot): Boolean {
        throw UnsupportedOperationException()
    }
    override fun kInstanceStop(tmodel: ContainerRoot): Boolean {
        throw UnsupportedOperationException()
    }
    override fun kUpdateDictionary(d: Map<String, Any>, cmodel: ContainerRoot): Map<String, Any>? {
        throw UnsupportedOperationException()
    }

}