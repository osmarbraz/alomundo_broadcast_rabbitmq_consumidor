import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * Exemplo de recebimento (Consumidor) de mensagens de uma Exchange
 * do tipo fanout(Broadcast) do RabbitMQ utilizando o serviço CloudAMQP.
 *
 * O consumidor possui uma fila própria, que é vinculada à Exchange.
 * Dessa forma, recebe uma cópia das mensagens publicadas na Exchange.
 */
public class Principal {

    // Nome da Exchange que será utilizada para receber as mensagens.
    private static final String EXCHANGE = "demo.fanout";

    // URL de conexão com o servidor RabbitMQ.
    private static final String URL_RABBITMQ =  "amqps://usuario:senha@host/virtualhost";

    public static void main(String[] args) {

        /*
         * Verifica se o nome da fila foi informado como argumento
         * na execução do programa.
         *
         * O consumidor utiliza uma fila própria para receber as
         * mensagens encaminhadas pela Exchange fanout.
         */
        if (args.length == 0) {
            System.err.println("Uso: java Principal <queueName>");
            System.exit(1);
        }

        // Obtém o nome da fila informado como argumento.
        String nomeFila = args[0];

        try {

            // Cria a fábrica responsável por estabelecer conexões
            // com o servidor RabbitMQ.
            ConnectionFactory factory = new ConnectionFactory();

            try {

                // Configura a conexão utilizando a URL do RabbitMQ.
                factory.setUri(URL_RABBITMQ);

                /*
                 * Estabelece a conexão com o servidor RabbitMQ
                 * e cria um canal de comunicação.
                 *
                 * O canal será utilizado para declarar a Exchange,
                 * criar a fila, estabelecer o vínculo e consumir
                 * as mensagens.
                 */
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();

                /*
                 * Declara a Exchange no RabbitMQ.
                 *
                 * EXCHANGE:
                 * Nome da Exchange que será utilizada.
                 *
                 * "fanout":
                 * Define o tipo da Exchange. Nesse modelo, a mensagem
                 * é encaminhada para todas as filas vinculadas à Exchange.
                 *
                 * true:
                 * Define a Exchange como durável, mantendo-a após
                 * uma reinicialização do servidor RabbitMQ.
                 */
                channel.exchangeDeclare(EXCHANGE, "fanout", true);

                /*
                 * Define as características da fila.
                 *
                 * durable = true:
                 * A fila será mantida mesmo após uma reinicialização
                 * do servidor RabbitMQ.
                 *
                 * exclusive = false:
                 * A fila não pertence exclusivamente à conexão atual.
                 *
                 * autoDelete = false:
                 * A fila não será excluída automaticamente.
                 */
                boolean durable = true;
                boolean exclusive = false;
                boolean autoDelete = false;

                /*
                 * Declara a fila no RabbitMQ.
                 *
                 * O nome da fila é obtido por meio do argumento
                 * informado na execução do programa.
                 */
                channel.queueDeclare(
                        nomeFila,
                        durable,
                        exclusive,
                        autoDelete,
                        null
                );

                /*
                 * Cria um vínculo (binding) entre a fila e a Exchange.
                 *
                 * A partir desse vínculo, as mensagens publicadas
                 * na Exchange fanout serão encaminhadas para essa fila.
                 *
                 * A routing key é ignorada pelas Exchanges do tipo fanout,
                 * por isso é utilizada uma string vazia.
                 */
                channel.queueBind(nomeFila, EXCHANGE, "");

                /*
                 * Exibe uma mensagem informando que o consumidor
                 * está conectado à fila e aguardando mensagens.
                 */
                System.out.printf("Consumindo da fila '%s'. CTRL+C para sair.%n", nomeFila);

                /*
                 * Define o comportamento executado quando uma
                 * mensagem for recebida.
                 */
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                    // Converte os bytes recebidos para texto utilizando UTF-8.
                    String mensagem = new String(delivery.getBody(),StandardCharsets.UTF_8);

                    // Exibe a mensagem recebida no console.
                    System.out.printf(" [%s] Recebido: %s%n", nomeFila, mensagem);
                };

                /*
                 * Inicia o consumidor.
                 *
                 * true indica que será utilizada confirmação
                 * automática das mensagens.
                 *
                 * O consumidor permanecerá ativo aguardando
                 * novas mensagens.
                 */
                channel.basicConsume(nomeFila, true, deliverCallback, consumerTag -> {} );

            } catch (URISyntaxException ex) {
                // Trata erros relacionados ao formato da URL.
                System.err.println("Erro: " + ex.getMessage());
            } catch (NoSuchAlgorithmException ex) {
                // Trata erros relacionados ao algoritmo de segurança
                // utilizado na conexão.
                System.err.println("Erro: " + ex.getMessage());
            } catch (KeyManagementException ex) {
                // Trata erros relacionados ao gerenciamento das chaves
                // de segurança da conexão.
                System.err.println("Erro: " + ex.getMessage());
            }
        } catch (IOException | TimeoutException | RuntimeException e) {
            // Exibe uma mensagem de erro caso ocorra algum problema
            // durante a conexão ou o recebimento das mensagens.
            System.err.println("Erro ao receber mensagens: " + e.getMessage());
        }
    }
}