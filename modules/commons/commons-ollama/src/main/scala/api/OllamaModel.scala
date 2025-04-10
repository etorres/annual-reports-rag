package es.eriktorr
package api

enum OllamaModel(val name: String):
  case DeepSeekR1 extends OllamaModel("deepseek-r1")
  case Gemma3 extends OllamaModel("gemma3")
  case Llama3_2 extends OllamaModel("llama3.2")
  case Mistral extends OllamaModel("mistral")
  case Phi4 extends OllamaModel("phi4")
  case QwQ extends OllamaModel("qwq")
  case TinyLlama extends OllamaModel("tinyllama")
