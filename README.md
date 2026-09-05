# Consumidor RabbitMQ com Java — Broadcast

Aplicação Java que atua como **consumidora de mensagens** utilizando o **RabbitMQ** e o serviço **CloudAMQP**.

A aplicação utiliza uma **Exchange do tipo `fanout`** para receber mensagens. Cada consumidor possui uma **fila própria**, que é vinculada à Exchange. Dessa forma, todas as mensagens publicadas na Exchange são encaminhadas para as filas vinculadas.

Neste aplicação temos N filas, 1 produtor e N consumidores.

## Tecnologias utilizadas

* **Java**
* **RabbitMQ**
* **CloudAMQP**
* **RabbitMQ Java Client**
* **Maven**
* **AMQPS**
* **UTF-8**

## Funcionamento

A aplicação:

1. Estabelece uma conexão segura com o RabbitMQ utilizando **AMQPS**.
2. Cria um canal de comunicação.
3. Declara a Exchange `demo.fanout`.
4. Configura a Exchange como do tipo **`fanout`**.
5. Recebe o nome da fila por meio de um argumento da linha de comando.
6. Declara a fila do consumidor.
7. Cria um vínculo (**binding**) entre a fila e a Exchange.
8. Aguarda novas mensagens.
9. Exibe as mensagens recebidas no console.

## Exchange Fanout

A aplicação utiliza a Exchange:

```text
demo.fanout
```
### Fluxo da comunicação

```text
                    +--> Fila A --> Consumidor A
                    |
Produtor --> demo.fanout --> Fila B --> Consumidor B
                    |
                    +--> Fila C --> Consumidor C
```

## Execução

Configure a URL de conexão do RabbitMQ no código:

```java
private static final String URL_RABBITMQ = "...";
```

Depois, compile o projeto:

```bash
mvn clean package
```

Antes de executar o produtor, é necessário abrir múltiplas instâncias da aplicação consumidora.

Cada instância deve receber como argumento o nome de uma fila diferente.

Por exemplo, abra três terminais e execute:

Terminal 1:

```bash
mvn exec:java -Dexec.args="fila1"
```

Terminal 2:

```bash
mvn exec:java -Dexec.args="fila2"
```

Terminal 3:

```bash
mvn exec:java -Dexec.args="fila3"
```

Cada instância ficará conectada à sua respectiva fila e aguardará novas mensagens.

Somente depois de iniciar as instâncias dos consumidores, execute a aplicação produtora.

O produtor publicará uma mensagem na Exchange

## Observação

A URL do RabbitMQ contém credenciais de acesso. **Não publique credenciais reais no código-fonte ou em repositórios públicos.** Prefira utilizar variáveis de ambiente ou arquivos de configuração seguros.

## Aplicação produtora

https://github.com/osmarbraz/alomundo_broadcast_rabbitmq_produtor
