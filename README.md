# MJC - MiniJava Compiler

Compilador para a linguaguem MiniJava

## Módulos Concluídos

|                    Módulo                     |                                                                                                                 Explicação                                                                                                                 |
|:---------------------------------------------:|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|               Analisador Léxico               |                               Responsável por ler o código-fonte e convertê-lo em uma sequência de *tokens*. Esta é a primeira etapa da compilação, onde o texto é transformado em unidades significativas.                                |
| Árvore Sintática Abstrata e Análise Semântica | A Árvore Sintática Abstrata (AST) representa a estrutura hierárquica do código, ignorando detalhes de sintaxe. A análise semântica valida o uso correto das variáveis, tipos e escopos, garantindo que o programa faz sentido logicamente. |
|      Tradução Para Código Intermediário       |                                      Converte a AST em um formato intermediário, facilitando otimizações e posterior tradução para código de máquina. Esse código é independente da arquitetura alvo.                                      |
|             Seleção de Instruções             |                                     Transforma o código intermediário em instruções específicas da arquitetura alvo, como assembly. É a etapa final da tradução antes da geração de código executável.                                     |

## Tecnologias

- Java 21
- Antlr4 (para análise léxica e sintática)
- Maven (para gerenciamento de dependências e construção do projeto)
- JUnit (para testes)
- Log4j2 (para logging)

## Estrutura

- `src/main`: código-fonte principal
  - `/antlr`: gramática ANTLR
  - `/java/org/mjc`: código Java do compilador
  - `/resources`: configuração de logs
- `src/test`: classes de teste e arquivos de exemplo
- `target/`: arquivos compilados
- `target/generated-sources/antlr`: arquivos gerados pelo ANTLR

## Como executar

1. Clone o repositório


2. Maven clean install para compilar o projeto:
   ```bash
    mvn clean install
    ```

3. Marcar /target/generated-sources como fonte na IDE:
   - No IntelliJ, vá em `File -> Project Structure -> Modules -> Sources`
   e marque o diretório `target/generated-sources/antlr` como source root
   - No Eclipse, clique com o botão direito no projeto, vá em
   `Properties -> Java Build Path -> Source` e adicione o diretório `target/generated-sources/antlr`
   - No NetBeans, clique com o botão direito no projeto, vá em `Properties -> Sources` e adicione o
   diretório `target/generated-sources/antlr`
   - No VsCode, adicione o caminho target/generated-sources/antlr ao seu java.project.sourcePaths no settings.json


4. Execute os testes ou o compilador diretamente na classe Main
