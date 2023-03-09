package com.alexexample.consumer

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy
import org.opensearch.client.RequestOptions.DEFAULT
import org.opensearch.client.RestClient
import org.opensearch.client.RestHighLevelClient
import org.opensearch.client.indices.CreateIndexRequest
import org.opensearch.client.indices.GetIndexRequest
import org.slf4j.Logger
import java.net.URI

private const val CONNECTION_URL = "http://localhost:9200"
private const val SCHEMA = "http"

internal fun restHighLevelClient(): RestHighLevelClient =
    URI.create(CONNECTION_URL).let { uri ->
        if (uri.userInfo == null)
            RestHighLevelClient(RestClient.builder(HttpHost(uri.host, uri.port, SCHEMA)))
        else
            uri.userInfo.split(":").let { auth ->
                BasicCredentialsProvider().apply {
                    setCredentials(AuthScope.ANY, UsernamePasswordCredentials(auth[0], auth[1]))
                }.let { credentials ->
                    RestHighLevelClient(RestClient.builder(HttpHost(uri.host, uri.port, uri.scheme)).apply {
                        setHttpClientConfigCallback { httpAsyncClientBuilder ->
                            httpAsyncClientBuilder.apply {
                                setDefaultCredentialsProvider(credentials)
                                setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy())
                            }
                        }
                    })
                }
            }
    }

internal fun createIndex(restHighLevelClient: RestHighLevelClient, index: String, logger: Logger) {
    restHighLevelClient.indices().also {
        if (it.exists(GetIndexRequest(index), DEFAULT))
            logger.info("index with name: $index is already exists")
        else
            it.create(CreateIndexRequest(index), DEFAULT).also {
                logger.info("index with name: $index is created")
            }
    }
}
