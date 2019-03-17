package org.leacar21.examples.threading.futures;

import java.security.ProviderException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FeatureExample {

	// Pool fijo con 5 hilos y una cola
	private final ExecutorService executor = Executors.newFixedThreadPool(5);
	
	// Pool con 4 cores iniciales que crece hasta 8 nodos si se llena la cola de largo MAX_VALUE. 
	private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 8, 60, TimeUnit.SECONDS, new ArrayBlockingQueue(Integer.MAX_VALUE));
	
	
    public void completableFeatureBasic() {

        // CREACIÓN
        // Hay varias formas de crear un CompletableFeature

        // Con la nueva clase CompletableFuture podemos crear el futuro ya directamente “completo”, con el valor, con el método estático ‘completedFuture‘.
        CompletableFuture<String> future1 = CompletableFuture.completedFuture("Prueba");

        // También podemos crearla directamente, con un new. Más adelante podemos simplemente “completar” el futuro, con el método ‘complete‘.
        CompletableFuture<String> future2 = new CompletableFuture<>();
        future2.complete("Completado!");

        // Podemos crear un futuro que ejecute un pequeño proceso, como vemos, pasándole directamente una función lambda. En el usamos el
        // método ‘runAsync‘, aunque tenemos más opciones.
        CompletableFuture<Void> futureAsync = CompletableFuture.runAsync(() ->
            {
                // Some stuff...
            });

        // VARIEDAD DE MÉTODOS

        // Al ver el API de la clase CompletableFuture, vemos que nos proporciona muchos métodos, casi todos ellos en tres “versiones”.
        // Por ejemplo, para el método ‘thenAccept‘ tenemos:
        // CompletableFuture<Void> thenAccept(Consumer<? super T> action);
        // CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action);
        // CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor);

        // Lo normal si buscamos un buen sistema ‘reactivo’ sería usar siempre la versión con “Async” (http://www.reactivemanifesto.org/es)

        // MÉTODOS RUN y SUPPLY
        // La manera mas común de crear un CompletableFeature es pasandole una función lambda. Para esto tendremos dos métodos: runAsync y supplyAsync

        // runAsync se usa cuando NO se requiere que el procesamiento a ejecutar en paralelo retorne un valor.
        // supplyAsync se utiliza cuando se requiere que el procesamiento a ejecutar en paralelo retorne un valor.

        CompletableFuture<Void> futureRunAsync = CompletableFuture.runAsync(() ->
            {
                System.out.println("Comenzando runAsync...");
                this.sleepSeconds(3);
                System.out.println("Terminado runAsync!");
            } , this.executor);

        CompletableFuture<String> futureSupplyAsync = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando supplyAsync...");
                this.sleepSeconds(3);
                System.out.println("Terminado supplyAsync!");
                return "Terminado";
            } , this.executor);
            // Usamos la interfaz ExecutorService para “lanzar” un Callable (con un lambda de Java 8). Para eso necesitamos haber definido el Executor, que es
            // una clase que se encarga de gestionar pool de threads.

        // ¿COMO OBTENER EL RESULTADO?

        // Para obtener el resultado tenemos siempre la posibilidad de “bloquearnos” en el futuro, llamando al método ‘get‘.
        try {
            System.out.println("Resultado bloqueando supplyAsync: " + futureSupplyAsync.get());
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // También se nos ofrece un nuevo método ‘getNow‘ el cual lo que hace es, si el futuro se ha completado, devolver el resultado, y si no,
        // devolver un parámetro que le pasamos a ese método
        System.out.println("Resultado bloqueando supplyAsync: " + futureSupplyAsync.getNow("Todavia no termino el feature"));

        // Pero si ninguna de esas opciones es lo que queremos vease el método completableFeature2 que aparece mas abajo
        // en el que se ven los Listeners o Callbacks

    }

    // --------------------------------------------------------------------------------

    public void completableFeatureCallbacks() {
        // LISTENERS o CALLBACKS

        // -----

        // *** whenComplete: añade un callback para ejecutarlo cuando el futuro se complete

        // Ahora vamos a obtener el resultado sin bloquearnos, añadiendo algo así como un listener o callback a nuestro futuro.
        CompletableFuture<String> futureSupplyAsync2 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando supplyAsync...");
                this.sleepSeconds(3);
                System.out.println("Terminado supplyAsync!");
                return "Terminado";
            } , this.executor);

        futureSupplyAsync2.whenCompleteAsync((s, e) -> System.out.println("Resultado supplyAsync: " + s), this.executor);
        System.out.println("Terminado main thread");
        // La llamada al método ‘whenCompleteAsync‘ realmente no se bloquea en el futuro. Lo que hace es “registrar”
        // en el futuro que cuando se complete, ejecute esa función lambda. En 's' esta el resultado de la ejecución y en 'e' posibles excepciones

        // -----

        // *** thenApply: Sirve para transformar Futures. La idea es pasarle una función lambda que transforme el resultado del primero.

        CompletableFuture<String> futureAsync1 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando supplyAsync for thenApply...");
                this.sleepSeconds(2);
                System.out.println("Terminado supplyAsync for thenApply!");
                return "Terminado";
            } , this.executor);

        CompletableFuture<String> futureApply = futureAsync1.thenApplyAsync(s ->
            {
                System.out.println("Comenzando applyAsync...");
                this.sleepSeconds(2);
                System.out.println("Terminado applyAsync!");
                return s.toUpperCase();
            } , this.executor);

        futureApply.whenCompleteAsync((s, e) -> System.out.println("Resultado applyAsync: " + s), this.executor);

        // -----

        // *** thenAccept y thenRun: muy similares al whenComplete, ejecutaran el lambda una vez se complete el futuro. El primero recibe un resultado,
        // y el segundo no. Son equivalentes al supplyAsync y runAsync respectivamente.

        // thenAccept
        CompletableFuture<String> futureAsync2 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando supplyAsync for thenAccept...");
                this.sleepSeconds(2);
                System.out.println("Terminado supplyAsync for thenAccept!");
                return "Terminado";
            } , this.executor);

        futureAsync2.thenAcceptAsync(s ->
            {
                System.out.println("Comenzando thenAccept...");
                this.sleepSeconds(2);
                System.out.println("Terminado thenAccept!");
                System.out.println("Resultado: " + s);
            } , this.executor);

        // thenRun
        CompletableFuture<Void> futureRun = CompletableFuture.runAsync(() ->
            {
                System.out.println("Comenzando runAsync for thenRun...");
                this.sleepSeconds(2);
                System.out.println("Terminado runAsync for thenRun!");
            } , this.executor);

        futureRun.thenRunAsync(() ->
            {
                System.out.println("Comenzando thenRun...");
                this.sleepSeconds(2);
                System.out.println("Terminado thenRun!");
            } , this.executor);

        // -----
    }

    // --------------------------------------------------------------------------------

    // EXCEPTIONS

    public void completableFeatureExceptions() {

        // Tenemos varias maneras de gestionar las excepciones de Futures con la clase CompletableFuture, usando estos métodos:

        // *** exceptionally: registra un callback para gestionar la excepción. Recibe una lambda que solo tiene de parámetro
        // la excepción, debe retornar un valor del mismo tipo que el Future en el que se originó la excepción.

        CompletableFuture<String> futureAsync1 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando supplyAsync with exception 1...");
                this.sleepSeconds(2);
                System.out.println("Terminado supplyAsync with exception 1!");
                throw new RuntimeException("Error en el futuro 1");
            } , this.executor);

        CompletableFuture<String> futureEx = futureAsync1.exceptionally(e ->
            {
                System.out.println("Resultado con excepción 1!! " + e.getMessage());
                return "StringPorDefecto1";
            });

        futureEx.whenCompleteAsync((s, e) -> System.out.println("Resultado futureEx 1: " + s), this.executor);

        // --------------------------------------------------------------------------------

        // *** handle: registra un callback para gestionar el resultado o excepción. Recibe una lambda que tiene dos parámetros,
        // el resultado y la excepción. Si la excepción no es nula, es que ha habido una excepción. También deber retornar un valor
        // del tipo del futuro que lanzo la excepción.

        CompletableFuture<String> futureAsync2 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando supplyAsync with exception 2...");
                this.sleepSeconds(2);
                System.out.println("Terminado supplyAsync with exception 2!");
                throw new RuntimeException("Error en el futuro 2");
            } , this.executor);

        CompletableFuture<String> handledFuture = futureAsync2.handleAsync((s, e) ->
            {
                if (e != null) {
                    System.out.println("Resultado con excepción 2!! " + e.getMessage());
                    return "StringPorDefecto2";
                } else {
                    System.out.println("Resultado2: " + s);
                    return s;
                }
            } , this.executor);

        handledFuture.whenCompleteAsync((s, e) -> System.out.println("Resultado handle 2: " + s), this.executor);

        // *** whenComplete: con este método que ya se explicó podemos hacer algo parecido al ‘handle’, dado que la lambda que registra tiene también los dos
        // parámetros.
        CompletableFuture<String> futureAsync = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando supplyAsync with exception 3...");
                this.sleepSeconds(2);
                System.out.println("Terminado supplyAsync with exception 3!");
                throw new RuntimeException("Error en el futuro 3");
            } , this.executor);

        futureAsync.whenCompleteAsync((s, e) ->
            {
                if (e != null) {
                    System.out.println("Resultado con excepción 3!! " + e);
                } else {
                    System.out.println("Resultado applyAsync 3: " + s);
                }
            } , this.executor);

    }

    // --------------------------------------------------------------------------------

    // COMBINAR FUTURES

    public void completableFeatureCombine() {

        // En casi todos los ejemplos anteriores prácticamente solo hemos usado callbacks sobre un mismo futuro. Pero, como hemos visto en algún
        // ejemplo (el ‘thenApply’), podemos encadenar futuros y combinarlos. En esta funcionalidad es donde se ve el verdadero potencial del
        // desarrollo usando futuros.

        // Tenemos los siguientes métodos:

        CompletableFuture<String> futureA11 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando supplyAsync for thenCompose...");
                this.sleepSeconds(2);
                System.out.println("Terminado supplyAsync for thenCompose!");
                return "Terminado";
            } , this.executor);

        CompletableFuture<String> fCompose = futureA11.thenComposeAsync(s -> CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando thenCompose...");
                this.sleepSeconds(2);
                System.out.println("Terminado thenCompose!");
                return s.concat(" + Terminado other");
            } , this.executor), this.executor);

        fCompose.whenCompleteAsync((s, e) -> System.out.println("Resultado thenCompose: " + s), this.executor);

        // --------------------------------------------------------------------------------

        // *** thenCombine: En este caso, en lugar de una cadena de futuros, espera a que terminen dos futuros, para luego hacer algo. En este caso la
        // lambda tendrá dos parámetros, que son el resultado de cada uno de los dos futuros:

        CompletableFuture<String> futureA12 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando future1 for thenCombine...");
                this.sleepSeconds(2);
                System.out.println("Terminado future1 for thenCombine!");
                return "Terminado";
            } , this.executor);

        CompletableFuture<String> futureB12 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando future2 for thenCombine...");
                this.sleepSeconds(1);
                System.out.println("Terminado future2 for thenCombine!");
                return "Terminado other";
            } , this.executor);

        CompletableFuture<String> fCombine = futureA12.thenCombineAsync(futureB12, (s1, s2) ->
            {
                System.out.println("En el thenCombine, recibidos results: " + s1 + " " + s2);
                return s1 + s2;
            } , this.executor);

        fCombine.whenCompleteAsync((s, e) -> System.out.println("Resultado thenCombine: " + s), this.executor);

        // *** thenAcceptBoth y runAfterBoth: Muy similares al ‘thenCombine’, excepto que no generan un nuevo futuro,
        // simplemente ejecutan la lambda cuando los dos futuros terminen. Es como un ‘whenComplete‘ pero esperando dos futuros:

        // thenAcceptBoth
        CompletableFuture<String> futureA13 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando future1 for thenAcceptBoth...");
                this.sleepSeconds(2);
                System.out.println("Terminado future1 for thenAcceptBoth!");
                return "Terminado";
            } , this.executor);

        CompletableFuture<String> futureB13 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando future2 for thenAcceptBoth...");
                this.sleepSeconds(1);
                System.out.println("Terminado future2 for thenAcceptBoth!");
                return "Terminado other";
            } , this.executor);

        futureA13.thenAcceptBothAsync(futureB13, (s1, s2) -> System.out.println("En el thenAcceptBoth, recibidos results:" + s1 + " " + s2), this.executor);

        // runAfterBoth
        CompletableFuture<Void> futureA23 = CompletableFuture.runAsync(() ->
            {
                System.out.println("Comenzando future1 for runAfterBoth...");
                this.sleepSeconds(2);
                System.out.println("Terminado future1 for runAfterBoth!");
            } , this.executor);

        CompletableFuture<Void> futureB23 = CompletableFuture.runAsync(() ->
            {
                System.out.println("Comenzando future2 for runAfterBoth...");
                this.sleepSeconds(1);
                System.out.println("Terminado future2 for runAfterBoth!");
            } , this.executor);

        futureA23.runAfterBothAsync(futureB23, () -> System.out.println("En el runAfterBoth, futuros terminados."), this.executor);

        // *** acceptEither y runAfterEither: En algunos casos en que tengamos dos futuros nos interesará hacer algo cuando uno
        // de los dos termine, el primero que lo haga. Para eso están estos dos métodos:

        // acceptEither
        CompletableFuture<String> futureA14 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando future1 for acceptEither...");
                this.sleepSeconds(3);
                System.out.println("Terminado future1 for acceptEither!");
                return "Segundo";
            } , this.executor);

        CompletableFuture<String> futureB14 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando future2 for acceptEither...");
                this.sleepSeconds(1);
                System.out.println("Terminado future2 for acceptEither!");
                return "Primero";
            } , this.executor);

        futureA14.acceptEitherAsync(futureB14, (s) -> System.out.println("En el acceptEither, recibido el primer resultado: " + s), this.executor);

        // runAfterEither
        CompletableFuture<Void> futureA24 = CompletableFuture.runAsync(() ->
            {
                System.out.println("Comenzando future1 for runAfterEither...");
                this.sleepSeconds(3);
                System.out.println("Terminado future1 for runAfterEither!");
            } , this.executor);

        CompletableFuture<Void> futureB24 = CompletableFuture.runAsync(() ->
            {
                System.out.println("Comenzando future2 for runAfterEither...");
                this.sleepSeconds(1);
                System.out.println("Terminado future2 for runAfterEither!");
            } , this.executor);

        futureA24.runAfterEitherAsync(futureB24, () -> System.out.println("En el runAfterEither, primero terminado."), this.executor);

        // *** applyToEither: muy similar a ‘acceptEither’, pero este devuelve a su vez un futuro. Es como el ‘thenApply’ pero sobre el
        // futuro que termine antes:
        CompletableFuture<String> futureA15 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando future1 for applyToEither...");
                this.sleepSeconds(3);
                System.out.println("Terminado future1 for applyToEither!");
                return "Segundo";
            } , this.executor);

        CompletableFuture<String> futureB15 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando future2 for applyToEither...");
                this.sleepSeconds(1);
                System.out.println("Terminado future2 for applyToEither!");
                return "Primero";
            } , this.executor);

        CompletableFuture<String> applyToEitherFuture = futureA15.applyToEitherAsync(futureB15, s ->
            {
                System.out.println("Comenzando applyToEither...");
                this.sleepSeconds(1);
                System.out.println("Terminado applyToEither!");
                return s.toUpperCase();
            } , this.executor);

        applyToEitherFuture.whenCompleteAsync((s, e) -> System.out.println("Resultado applyToEither: " + s), this.executor);

        // allOf y anyOf: Con estos dos métodos podemos hacer un ‘thenAcceptBoth’ o ‘acceptEither’ sobre un número ilimitado de futuros:
        // allOf
        CompletableFuture<String> futureA16 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando future1 for allOf...");
                this.sleepSeconds(2);
                System.out.println("Terminado future1 for allOf!");
                return "Terminado future1";
            } , this.executor);

        CompletableFuture<String> futureB16 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando future2 for allOf...");
                this.sleepSeconds(1);
                System.out.println("Terminado future2 for allOf!");
                return "Terminado future2";
            } , this.executor);

        CompletableFuture<String> futureC16 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando future3 for allOf...");
                this.sleepSeconds(3);
                System.out.println("Terminado future3 for allOf!");
                return "Terminado future3";
            } , this.executor);

        CompletableFuture<Void> all1 = CompletableFuture.allOf(futureA16, futureB16, futureC16);

        all1.whenCompleteAsync((s, e) -> System.out.println("Resultado all: " + s), this.executor);

        // anyOf
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando future1 for allOf...");
                this.sleepSeconds(2);
                System.out.println("Terminado future1 for allOf!");
                return "Terminado future1";
            } , this.executor);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando future2 for allOf...");
                this.sleepSeconds(1);
                System.out.println("Terminado future2 for allOf!");
                return "Terminado future2";
            } , this.executor);

        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() ->
            {
                System.out.println("Comenzando future3 for allOf...");
                this.sleepSeconds(3);
                System.out.println("Terminado future3 for allOf!");
                return "Terminado future3";
            } , this.executor);

        CompletableFuture<Object> all2 = CompletableFuture.anyOf(future1, future2, future3);

        all2.whenCompleteAsync((s, e) -> System.out.println("Resultado any: " + s), this.executor);

    }

    // --------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------

    public List<Integer> completableFeatureExample1() {

        System.out.println("Run 1");
        CompletableFuture<List<Integer>> cfNumbers1 = this.getList(1, 3);

        System.out.println("Run 2");
        CompletableFuture<List<Integer>> cfNumbers2 = this.getList(2, 6);

        System.out.println("Wait...");

        List<Integer> numbers1 = this.getList(cfNumbers1);
        System.out.println("Complete 1");

        List<Integer> numbers2 = this.getList(cfNumbers2);
        System.out.println("Complete 2");

        return numbers1;
    }

    // --------------------------------------------------------------------------------

    //
    private CompletableFuture<List<Integer>> getList(Integer num, int sleep) {
        return CompletableFuture.supplyAsync(() ->
            {
                List<Integer> numbers = Arrays.asList(num, num + 1, num + 2);
                this.sleepSeconds(sleep);
                System.out.println("Antes del Retornar de getList");
                return numbers;
            } , this.executor).handle((ok, ex) ->
                {
                    // Luego de ejecutarse la operación anonima, se ejecuta lo definido en handle
                    // En ok queda el resultado del return
                    if (ex != null) {
                        throw new ProviderException("Provider Name", ex);
                    }
                    return ok;
                });
    }

    // --------------------------------------------------------------------------------

    // Con el metodo get del completableFeature obtenemos el objeto resultado de la operación que se ejecuto en otro hilo
    private List<Integer> getList(CompletableFuture<List<Integer>> list) {
        try {
            long SECONDS_TIMEOUT = 20;
            return list.get(SECONDS_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.out.println("Exception Timeout");
            throw new ProviderException("Provider Name", e);
        }
    }

    // --------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------

    private void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
}
