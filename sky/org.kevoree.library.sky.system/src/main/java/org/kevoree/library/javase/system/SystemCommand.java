package org.kevoree.library.javase.system;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.log.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/03/12
 * Time: 11:24
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
@DictionaryType({
		@DictionaryAttribute(name = "START_COMMAND", optional = false),
		@DictionaryAttribute(name = "STOP_COMMAND", optional = false)
})
public class SystemCommand extends AbstractComponentType {

	@Start
	public void startSystemComponent () throws Exception {
		Log.debug("Starting system component with command: {}", this.getDictionary().get("START_COMMAND").toString());
		Process p = Runtime.getRuntime().exec(this.getDictionary().get("START_COMMAND").toString());
		new Thread(new ProcessStreamFileLogger(p.getInputStream())).start();
		new Thread(new ProcessStreamFileLogger(p.getErrorStream())).start();
		if (p.waitFor() != 0) {
			throw new Exception("Unable to execute command: " +  this.getDictionary().get("START_COMMAND").toString());
		}
	}

	@Stop
	public void stopSystemComponent () throws Exception {
        Log.debug("Stopping system component with command: {}", this.getDictionary().get("STOP_COMMAND").toString());
		Process p = Runtime.getRuntime().exec(this.getDictionary().get("STOP_COMMAND").toString());
		new Thread(new ProcessStreamFileLogger(p.getInputStream())).start();
		new Thread(new ProcessStreamFileLogger(p.getErrorStream())).start();
		if (p.waitFor() != 0) {
			throw new Exception("Unable to execute command: " +  this.getDictionary().get("STOP_COMMAND").toString());
		}
	}


	private class ProcessStreamFileLogger implements Runnable {
		private InputStream inputStream;

		ProcessStreamFileLogger (InputStream inputStream) {
			this.inputStream = inputStream;
		}

		public void run () {
			BufferedReader readerIn = null;
			try {
				readerIn = new BufferedReader(new InputStreamReader(inputStream));
				String lineIn = readerIn.readLine();
				while (lineIn != null) {
                    Log.trace(lineIn);

					lineIn = readerIn.readLine();
				}
			} catch (IOException ignored) {
			} finally {
				if (readerIn != null) {
					try {
						readerIn.close();
					} catch (IOException ignored) {
					}
				}
			}
		}
	}
}
