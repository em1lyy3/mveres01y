/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.rpc.config;

import com.alipay.sofa.rpc.api.GenericService;
import com.alipay.sofa.rpc.bootstrap.Bootstraps;
import com.alipay.sofa.rpc.bootstrap.ConsumerBootstrap;
import com.alipay.sofa.rpc.client.Router;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.common.annotation.Unstable;
import com.alipay.sofa.rpc.common.utils.ClassUtils;
import com.alipay.sofa.rpc.common.utils.CommonUtils;
import com.alipay.sofa.rpc.common.utils.ExceptionUtils;
import com.alipay.sofa.rpc.common.utils.StringUtils;
import com.alipay.sofa.rpc.core.invoke.SofaResponseCallback;
import com.alipay.sofa.rpc.listener.ChannelListener;
import com.alipay.sofa.rpc.listener.ConsumerStateListener;
import com.alipay.sofa.rpc.listener.ProviderInfoListener;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import static com.alipay.sofa.rpc.common.RpcConfigs.getBooleanValue;
import static com.alipay.sofa.rpc.common.RpcConfigs.getIntValue;
import static com.alipay.sofa.rpc.common.RpcConfigs.getStringValue;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_ADDRESS_HOLDER;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_ADDRESS_WAIT;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_CHECK;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_CLUSTER;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_CONCURRENTS;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_CONNECTION_HOLDER;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_CONNECTION_NUM;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_CONNECT_TIMEOUT;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_DISCONNECT_TIMEOUT;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_HEARTBEAT_PERIOD;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_INJVM;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_INVOKE_TYPE;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_LAZY;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_LOAD_BALANCER;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_RECONNECT_PERIOD;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_REPEATED_REFERENCE_LIMIT;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_RETRIES;
import static com.alipay.sofa.rpc.common.RpcOptions.CONSUMER_STICKY;
import static com.alipay.sofa.rpc.common.RpcOptions.DEFAULT_PROTOCOL;

/**
 * ?????????????????????
 *
 * @param <T> the type parameter
 * @author <a href=mailto:zhanggeng.zg@antfin.com>GengZhang</a>
 */
public class ConsumerConfig<T> extends AbstractInterfaceConfig<T, ConsumerConfig<T>> implements Serializable {

    /**
     * The constant serialVersionUID.
     */
    private static final long                       serialVersionUID   = 4244077707655448146L;

    /**
     * ???????????????
     */
    protected String                                protocol           = getStringValue(DEFAULT_PROTOCOL);

    /**
     * ??????????????????
     */
    protected String                                directUrl;

    /**
     * ??????????????????
     */
    protected boolean                               generic;

    /**
     * ??????????????????
     */
    protected String                                invokeType         = getStringValue(CONSUMER_INVOKE_TYPE);

    /**
     * ??????????????????
     */
    protected int                                   connectTimeout     = getIntValue(CONSUMER_CONNECT_TIMEOUT);

    /**
     * ??????????????????????????????????????????????????????????????????????????????
     */
    protected int                                   disconnectTimeout  = getIntValue(CONSUMER_DISCONNECT_TIMEOUT);

    /**
     * ????????????????????????failover
     */
    protected String                                cluster            = getStringValue(CONSUMER_CLUSTER);

    /**
     * The ConnectionHolder ???????????????
     */
    protected String                                connectionHolder   = getStringValue(CONSUMER_CONNECTION_HOLDER);

    /**
     * ???????????????
     */
    protected String                                addressHolder      = getStringValue(CONSUMER_ADDRESS_HOLDER);

    /**
     * ????????????
     */
    protected String                                loadBalancer       = getStringValue(CONSUMER_LOAD_BALANCER);

    /**
     * ?????????????????????????????????????????????????????????????????????????????????check???????????????check???lazy???????????????
     *
     * @see ConsumerConfig#check
     */
    protected boolean                               lazy               = getBooleanValue(CONSUMER_LAZY);

    /**
     * ??????????????????????????????????????????
     * change transport when current is disconnected
     */
    protected boolean                               sticky             = getBooleanValue(CONSUMER_STICKY);

    /**
     * ??????jvm???????????????provider???consumer??????????????????jvm??????????????????jvm????????????????????????
     */
    protected boolean                               inJVM              = getBooleanValue(CONSUMER_INJVM);

    /**
     * ?????????????????????????????????????????????????????????????????????????????????lazy???????????????check???lazy????????????)
     *
     * @see ConsumerConfig#lazy
     */
    protected boolean                               check              = getBooleanValue(CONSUMER_CHECK);

    /**
     * ???????????????????????????????????????????????????????????????????????????
     */
    protected int                                   connectionNum      = getIntValue(CONSUMER_CONNECTION_NUM);

    /**
     * Consumer???Provider??????????????????
     */
    protected int                                   heartbeatPeriod    = getIntValue(CONSUMER_HEARTBEAT_PERIOD);

    /**
     * Consumer???Provider???????????????
     */
    protected int                                   reconnectPeriod    = getIntValue(CONSUMER_RECONNECT_PERIOD);

    /**
     * ??????????????????
     */
    protected List<String>                          router;

    /**
     * ???????????????????????????????????????????????????List<Router>
     */
    protected transient List<Router>                routerRef;

    /**
     * ??????????????????listener,????????????????????????
     */
    protected transient SofaResponseCallback        onReturn;

    /**
     * ?????????????????????????????????????????????????????????
     */
    @Unstable
    protected transient List<ChannelListener>       onConnect;

    /**
     * ????????????????????????????????????????????????????????????????????????
     */
    @Unstable
    protected transient List<ConsumerStateListener> onAvailable;

    /**
     * ?????????
     */
    protected String                                bootstrap;

    /**
     * ????????????????????????(??????)???-1??????????????????????????????
     */
    protected int                                   addressWait        = getIntValue(CONSUMER_ADDRESS_WAIT);

    /**
     * ??????????????????????????????uniqueId???????????????????????????????????????????????????bug?????????????????????????????????????????????????????????????????????-1???????????????
     *
     * @since 5.2.0
     */
    protected int                                   repeatedReferLimit = getIntValue(CONSUMER_REPEATED_REFERENCE_LIMIT);

    /*-------- ????????????????????????????????? --------*/
    /**
     * ???????????????????????????(??????)
     */
    protected int                                   timeout            = -1;

    /**
     * The Retries. ?????????????????????
     */
    protected int                                   retries            = getIntValue(CONSUMER_RETRIES);

    /**
     * ????????????????????????????????????????????????????????????-1??????????????????????????????0?????????????????????????????????
     */
    protected int                                   concurrents        = getIntValue(CONSUMER_CONCURRENTS);

    /*---------- ????????????????????? ------------*/

    /**
     * ????????????????????????
     */
    private transient ConsumerBootstrap<T>          consumerBootstrap;

    /**
     * ???????????????listener
     */
    private transient volatile ProviderInfoListener providerInfoListener;

    /**
     * Build key.
     *
     * @return the string
     */
    @Override
    public String buildKey() {
        return protocol + "://" + this.getInterfaceId() + ":" + uniqueId;
    }

    /**
     * Gets proxy class.
     *
     * @return the proxyClass
     */
    @Override
    public Class<?> getProxyClass() {
        if (proxyClass != null) {
            return proxyClass;
        }
        if (generic) {
            return GenericService.class;
        }
        try {
            if (StringUtils.isNotBlank(interfaceId)) {
                this.proxyClass = ClassUtils.forName(interfaceId);
                if (!RpcConstants.PROTOCOL_TYPE_TRIPLE.equals(protocol) && !proxyClass.isInterface()) {
                    throw ExceptionUtils.buildRuntime("consumer.interface",
                        interfaceId, "interfaceId must set interface class, not implement class");
                }
            } else {
                throw ExceptionUtils.buildRuntime("consumer.interface",
                    "null", "interfaceId must be not null");
            }
        } catch (RuntimeException t) {
            throw new IllegalStateException(t.getMessage(), t);
        }
        return proxyClass;
    }

    /**
     * Gets protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets protocol.
     *
     * @param protocol the protocol
     * @return the protocol
     */
    public ConsumerConfig<T> setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Gets directUrl.
     *
     * @return the directUrl
     */
    public String getDirectUrl() {
        return directUrl;
    }

    /**
     * Sets directUrl.
     *
     * @param directUrl the directUrl
     * @return the directUrl
     */
    public ConsumerConfig<T> setDirectUrl(String directUrl) {
        this.directUrl = directUrl;
        return this;
    }

    /**
     * Is generic boolean.
     *
     * @return the boolean
     */
    public boolean isGeneric() {
        return generic;
    }

    /**
     * Sets generic.
     *
     * @param generic the generic
     * @return the generic
     */
    public ConsumerConfig<T> setGeneric(boolean generic) {
        this.generic = generic;
        return this;
    }

    /**
     * Gets invoke type.
     *
     * @return the invoke type
     */
    public String getInvokeType() {
        return invokeType;
    }

    /**
     * Sets invoke type.
     *
     * @param invokeType the invoke type
     * @return the invoke type
     */
    public ConsumerConfig<T> setInvokeType(String invokeType) {
        this.invokeType = invokeType;
        return this;
    }

    /**
     * Gets connect timeout.
     *
     * @return the connect timeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets connect timeout.
     *
     * @param connectTimeout the connect timeout
     * @return the connect timeout
     */
    public ConsumerConfig<T> setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Gets disconnect timeout.
     *
     * @return the disconnect timeout
     */
    public int getDisconnectTimeout() {
        return disconnectTimeout;
    }

    /**
     * Sets disconnect timeout.
     *
     * @param disconnectTimeout the disconnect timeout
     * @return the disconnect timeout
     */
    public ConsumerConfig<T> setDisconnectTimeout(int disconnectTimeout) {
        this.disconnectTimeout = disconnectTimeout;
        return this;
    }

    /**
     * Gets cluster.
     *
     * @return the cluster
     */
    public String getCluster() {
        return cluster;
    }

    /**
     * Sets cluster.
     *
     * @param cluster the cluster
     * @return the cluster
     */
    public ConsumerConfig<T> setCluster(String cluster) {
        this.cluster = cluster;
        return this;
    }

    /**
     * Gets retries.
     *
     * @return the retries
     */
    public int getRetries() {
        return retries;
    }

    /**
     * Sets retries.
     *
     * @param retries the retries
     * @return the retries
     */
    public ConsumerConfig<T> setRetries(int retries) {
        this.retries = retries;
        return this;
    }

    /**
     * Gets connection holder.
     *
     * @return the connection holder
     */
    public String getConnectionHolder() {
        return connectionHolder;
    }

    /**
     * Sets connection holder.
     *
     * @param connectionHolder the connection holder
     * @return the connection holder
     */
    public ConsumerConfig<T> setConnectionHolder(String connectionHolder) {
        this.connectionHolder = connectionHolder;
        return this;
    }

    /**
     * Gets address holder.
     *
     * @return the address holder
     */
    public String getAddressHolder() {
        return addressHolder;
    }

    /**
     * Sets address holder.
     *
     * @param addressHolder the address holder
     * @return the address holder
     */
    public ConsumerConfig setAddressHolder(String addressHolder) {
        this.addressHolder = addressHolder;
        return this;
    }

    /**
     * Gets load balancer.
     *
     * @return the load balancer
     */
    public String getLoadBalancer() {
        return loadBalancer;
    }

    /**
     * Sets load balancer.
     *
     * @param loadBalancer the load balancer
     * @return the load balancer
     */
    public ConsumerConfig<T> setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
        return this;
    }

    /**
     * Is lazy boolean.
     *
     * @return the boolean
     */
    public boolean isLazy() {
        return lazy;
    }

    /**
     * Sets lazy.
     *
     * @param lazy the lazy
     * @return the lazy
     */
    public ConsumerConfig<T> setLazy(boolean lazy) {
        this.lazy = lazy;
        return this;
    }

    /**
     * Is sticky boolean.
     *
     * @return the boolean
     */
    public boolean isSticky() {
        return sticky;
    }

    /**
     * Sets sticky.
     *
     * @param sticky the sticky
     * @return the sticky
     */
    public ConsumerConfig<T> setSticky(boolean sticky) {
        this.sticky = sticky;
        return this;
    }

    /**
     * Is in jvm boolean.
     *
     * @return the boolean
     */
    public boolean isInJVM() {
        return inJVM;
    }

    /**
     * Sets in jvm.
     *
     * @param inJVM the in jvm
     * @return the in jvm
     */
    public ConsumerConfig<T> setInJVM(boolean inJVM) {
        this.inJVM = inJVM;
        return this;
    }

    /**
     * Is check boolean.
     *
     * @return the boolean
     */
    public boolean isCheck() {
        return check;
    }

    /**
     * Sets check.
     *
     * @param check the check
     * @return the check
     */
    public ConsumerConfig<T> setCheck(boolean check) {
        this.check = check;
        return this;
    }

    /**
     * Gets connectionNum.
     *
     * @return the connectionNum
     */
    public int getConnectionNum() {
        return connectionNum;
    }

    /**
     * Sets connectionNum.
     *
     * @param connectionNum the connectionNum
     * @return the connectionNum
     */
    public ConsumerConfig<T> setConnectionNum(int connectionNum) {
        this.connectionNum = connectionNum;
        return this;
    }

    /**
     * Gets heartbeatPeriod.
     *
     * @return the heartbeatPeriod
     */
    public int getHeartbeatPeriod() {
        return heartbeatPeriod;
    }

    /**
     * Sets heartbeatPeriod.
     *
     * @param heartbeatPeriod the heartbeatPeriod
     * @return the heartbeatPeriod
     */
    public ConsumerConfig<T> setHeartbeatPeriod(int heartbeatPeriod) {
        this.heartbeatPeriod = heartbeatPeriod;
        return this;
    }

    /**
     * Gets reconnectPeriod.
     *
     * @return the reconnectPeriod
     */
    public int getReconnectPeriod() {
        return reconnectPeriod;
    }

    /**
     * Sets reconnectPeriod.
     *
     * @param reconnectPeriod the reconnectPeriod
     * @return the reconnectPeriod
     */
    public ConsumerConfig<T> setReconnectPeriod(int reconnectPeriod) {
        this.reconnectPeriod = reconnectPeriod;
        return this;
    }

    /**
     * Gets router.
     *
     * @return the router
     */
    public List<String> getRouter() {
        return router;
    }

    /**
     * Sets router.
     *
     * @param router the router
     * @return the router
     */
    public ConsumerConfig setRouter(List<String> router) {
        this.router = router;
        return this;
    }

    /**
     * Gets routerRef.
     *
     * @return the routerRef
     */
    public List<Router> getRouterRef() {
        return routerRef;
    }

    /**
     * Sets routerRef.
     *
     * @param routerRef the routerRef
     * @return the routerRef
     */
    public ConsumerConfig<T> setRouterRef(List<Router> routerRef) {
        this.routerRef = routerRef;
        return this;
    }

    /**
     * Gets on return.
     *
     * @return the on return
     */
    public SofaResponseCallback getOnReturn() {
        return onReturn;
    }

    /**
     * Sets on return.
     *
     * @param onReturn the on return
     * @return the on return
     */
    public ConsumerConfig<T> setOnReturn(SofaResponseCallback onReturn) {
        this.onReturn = onReturn;
        return this;
    }

    /**
     * Gets on connect.
     *
     * @return the on connect
     */
    public List<ChannelListener> getOnConnect() {
        return onConnect;
    }

    /**
     * Sets on connect.
     *
     * @param onConnect the on connect
     * @return the on connect
     */
    public ConsumerConfig<T> setOnConnect(List<ChannelListener> onConnect) {
        this.onConnect = onConnect;
        return this;
    }

    /**
     * Gets on available.
     *
     * @return the on available
     */
    public List<ConsumerStateListener> getOnAvailable() {
        return onAvailable;
    }

    /**
     * Sets on available.
     *
     * @param onAvailable the on available
     * @return the on available
     */
    public ConsumerConfig<T> setOnAvailable(List<ConsumerStateListener> onAvailable) {
        this.onAvailable = onAvailable;
        return this;
    }

    /**
     * Gets timeout.
     *
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets timeout.
     *
     * @param timeout the timeout
     * @return the timeout
     */
    public ConsumerConfig<T> setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Gets concurrents.
     *
     * @return the concurrents
     */
    public int getConcurrents() {
        return concurrents;
    }

    /**
     * Sets concurrents.
     *
     * @param concurrents the concurrents
     * @return the concurrents
     */
    public ConsumerConfig<T> setConcurrents(int concurrents) {
        this.concurrents = concurrents;
        return this;
    }

    /**
     * Gets bootstrap.
     *
     * @return the bootstrap
     */
    public String getBootstrap() {
        return bootstrap;
    }

    /**
     * Sets bootstrap.
     *
     * @param bootstrap the bootstrap
     * @return the bootstrap
     */
    public ConsumerConfig<T> setBootstrap(String bootstrap) {
        this.bootstrap = bootstrap;
        return this;
    }

    /**
     * Gets address wait.
     *
     * @return the address wait
     */
    public int getAddressWait() {
        return addressWait;
    }

    /**
     * Sets address wait.
     *
     * @param addressWait the address wait
     * @return the address wait
     */
    public ConsumerConfig<T> setAddressWait(int addressWait) {
        this.addressWait = addressWait;
        return this;
    }

    /**
     * Gets max proxy count.
     *
     * @return the max proxy count
     */
    public int getRepeatedReferLimit() {
        return repeatedReferLimit;
    }

    /**
     * Sets max proxy count.
     *
     * @param repeatedReferLimit the max proxy count
     * @return the max proxy count
     */
    public ConsumerConfig<T> setRepeatedReferLimit(int repeatedReferLimit) {
        this.repeatedReferLimit = repeatedReferLimit;
        return this;
    }

    @Override
    public boolean hasTimeout() {
        if (timeout > 0) {
            return true;
        }
        if (CommonUtils.isNotEmpty(methods)) {
            for (MethodConfig methodConfig : methods.values()) {
                if (methodConfig.getTimeout() != null && methodConfig.getTimeout() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ???????????????????????????????????????????????????
     * ??????-1??????????????????????????????0?????????????????????????????????
     *
     * @return ???????????????concurrents boolean
     */
    @Override
    public boolean hasConcurrents() {
        if (concurrents > 0) {
            return true;
        }
        if (CommonUtils.isNotEmpty(methods)) {
            for (MethodConfig methodConfig : methods.values()) {
                if (methodConfig.getConcurrents() != null
                    && methodConfig.getConcurrents() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param methodName ?????????
     * @return ????????????????????? method retries
     */
    public int getMethodRetries(String methodName) {
        return (Integer) getMethodConfigValue(methodName, RpcConstants.CONFIG_KEY_RETRIES,
            getRetries());
    }

    /**
     * Gets the timeout corresponding to the method name
     *
     * @param methodName the method name
     * @return the time out
     */
    public int getMethodTimeout(String methodName) {
        return (Integer) getMethodConfigValue(methodName, RpcConstants.CONFIG_KEY_TIMEOUT,
            getTimeout());
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param methodName ???????????????????????????
     * @return method onReturn
     */
    public SofaResponseCallback getMethodOnreturn(String methodName) {
        return (SofaResponseCallback) getMethodConfigValue(methodName, RpcConstants.CONFIG_KEY_ONRETURN,
            getOnReturn());
    }

    /**
     * Gets the call type corresponding to the method name
     *
     * @param methodName the method name
     * @return the call type
     */
    public String getMethodInvokeType(String methodName) {
        return (String) getMethodConfigValue(methodName, RpcConstants.CONFIG_KEY_INVOKE_TYPE,
            getInvokeType());
    }

    /**
     * ????????????
     *
     * @return ??????????????? t
     */
    public T refer() {
        if (consumerBootstrap == null) {
            consumerBootstrap = Bootstraps.from(this);
        }
        return consumerBootstrap.refer();
    }

    /**
     * ??????????????????
     */
    public void unRefer() {
        if (consumerBootstrap != null) {
            consumerBootstrap.unRefer();
        }
    }

    /**
     * ??????????????????????????????
     *
     * @return ???????????????????????? consumer bootstrap
     */
    public ConsumerBootstrap<T> getConsumerBootstrap() {
        return consumerBootstrap;
    }

    /**
     * Sets serialization.
     *
     * @param serialization the serialization
     * @return the serialization
     */
    @Override
    public ConsumerConfig<T> setSerialization(String serialization) {
        this.serialization = serialization;
        return this;
    }

    /**
     * Gets provider info listener.
     *
     * @return the provider info listener
     */
    public ProviderInfoListener getProviderInfoListener() {
        return providerInfoListener;
    }

    /**
     * Sets provider info listener.
     *
     * @param providerInfoListener the provider info listener
     * @return the provider info listener
     */
    public ConsumerConfig<T> setProviderInfoListener(ProviderInfoListener providerInfoListener) {
        this.providerInfoListener = providerInfoListener;
        return this;
    }

    @Override
    public String getInterfaceId() {
        if (StringUtils.equals(RpcConstants.PROTOCOL_TYPE_TRIPLE, this.getProtocol())) {
            Class enclosingClass = this.getProxyClass().getEnclosingClass();
            Method sofaStub = null;
            String serviceName = interfaceId;
            try {
                sofaStub = enclosingClass.getDeclaredMethod("getServiceName");
                serviceName = (String) sofaStub.invoke(null);
            } catch (Throwable e) {
                //ignore
            }
            return serviceName;
        } else {
            return interfaceId;
        }
    }
}
