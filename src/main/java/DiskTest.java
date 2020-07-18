
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class DiskTest
{   
    public static void main(String[] args) throws IOException, InterruptedException
    {
    final String type = args[0];
        final String dir = args[1];
        final ExecutorService pool = Executors.newWorkStealingPool(Integer.valueOf(args[2]));
        
    if ("br".equalsIgnoreCase(type))
    {
        try (final Stream<Path> files = Files.find(
                        Paths.get(dir), Integer.MAX_VALUE, 
                        (p, bfa) -> bfa.isRegularFile() && p.getFileName().toString().matches("^timers.csv.*")))
        {
            files.forEach(f -> 
            {
                pool.submit(() -> 
                {
                    int charCount = 0;
                    try (final BufferedReader br = Files.newBufferedReader(f))
                    {
                        String line;
                        while ((line = br.readLine()) != null)
                        {
                            charCount += line.length();
                        }
                    }
                    catch (IOException e)
                    {
                    }
                });
            });
        }
    }
    else
    {
        try (final Stream<Path> files = Files.find(
                        Paths.get(dir), Integer.MAX_VALUE, 
                        (p, bfa) -> bfa.isRegularFile() && p.getFileName().toString().matches("^timers.csv.*")))
        {
            files.forEach(f -> 
            {
                pool.submit(() -> 
                {
                    byte[] buffer = new byte[1024];
                    
                    int charCount = 0;
                    try (final InputStream br = new BufferedInputStream(Files.newInputStream(f)))
                    {
                        int size = 0;
                        while ((size = br.read(buffer)) != -1)
                        {
                            charCount += size;
                        }
                    }
                    catch (IOException e)
                    {
                    }
                });
            });
        }
    }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.DAYS);
    }
}
