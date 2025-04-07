# Annual Reports RAG

[Ilya Rice: How I Won the Enterprise RAG Challenge](https://abdullin.com/ilya/how-to-build-best-rag/)

## Modules

### Report Indexing

* PDF parsing
* Text cleaning
* Chunking
* Store to vector databases

## Sample Files

Folder `round2/samples` in the [Enterprise RAG Challenge](https://github.com/trustbit/enterprise-rag-challenge) repository.

## Elasticsearch

```shell
curl -X GET "localhost:9200/_cat/indices?pretty&v&h=index,docs.count,store.size"
```
