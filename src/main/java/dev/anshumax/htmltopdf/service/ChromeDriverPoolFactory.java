package dev.anshumax.htmltopdf.service;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;

@CommonsLog
public class ChromeDriverPoolFactory extends BasePooledObjectFactory<ChromeDriver> {

    @Override
    public ChromeDriver create() {
        ChromeOptions options = new ChromeOptions();
        String[] args = {
                "--headless",
                "--disable-gpu",
                "--disable-dev-shm-usage",
                "--no-sandbox"
        };
        options.addArguments(args);

        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.default_content_settings.images", 2);
        options.setExperimentalOption("prefs", chromePrefs);
        return new ChromeDriver(options);
    }

    /**
     * Use the default PooledObject implementation.
     */
    @Override
    public PooledObject<ChromeDriver> wrap(ChromeDriver object) {
        return new DefaultPooledObject<>(object);
    }

    /**
     * When an object is returned to the pool, clear the buffer.
     */
    @Override
    public void passivateObject(PooledObject<ChromeDriver> pooledObject) {

    }

    @Override
    public void destroyObject(final PooledObject<ChromeDriver> p)  {
        p.getObject().quit();
    }

}
