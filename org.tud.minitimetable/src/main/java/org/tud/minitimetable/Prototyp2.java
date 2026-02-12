package org.tud.minitimetable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.tud.minitimetable.util.DataModelManager;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

public class Prototyp2 {

	private static class MZSolution {
		public final long timestamp;
		public final Object result;

		public MZSolution(long timestamp, Object result) {
			this.timestamp = timestamp;
			this.result = Objects.requireNonNull(result);
		}

		@Override
		public String toString() {
			return "Solution: " + result.toString();
		}

	}

	private static class MZOutputParser implements Closeable {
		private InputStream input;

		public MZOutputParser(InputStream mzOutput) {
			input = Objects.requireNonNull(mzOutput);
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub

		}
	}

	private static final Path miniZincLocation = Path.of("..", "..", "MiniZinc", "minizinc.exe");
	private static final Path resourceFolder = Path.of("./", "resources");

	private static Path changeFileExtension(Path filePath, String newExtension) {
		String fileName = filePath.getFileName().toString();
		int extensionDelimiter = fileName.lastIndexOf('.');

		if (extensionDelimiter > 0) {
			int newExtensionStart = newExtension.startsWith(".") ? 1 : 0;
			String newfileName = fileName.substring(0, extensionDelimiter + 1)
					+ newExtension.substring(newExtensionStart);
			return filePath.resolveSibling(newfileName);
		}

		return filePath;
	}

	private static Path getUserDirectory() {
		String currentDir = System.getProperty("user.dir");
		return Path.of(currentDir);
	}

	private static Path getResourceDirectory() {
		return getUserDirectory().resolve(resourceFolder).normalize();
	}

	private static Path getMiniZinc() {
		return getUserDirectory().resolve(miniZincLocation).normalize();
	}

	private static Path getValidator() {
		return getResourceDirectory().resolve("IHTP_Validator_win.exe");
	}

	private static void transformInput() throws IOException {
		Path modelFolder = getResourceDirectory().resolve("ihtc");
		Path outputFolder = getResourceDirectory().resolve("out");

		DataModelManager manager = new DataModelManager(modelFolder, outputFolder);
		manager.loadDataModel("i01");
		manager.writeDataModelAsDZN();

	}

	public static void main(String[] args) throws IOException, InterruptedException {
		var jsonOutputEnabled = false;

		Path constraintModel = getResourceDirectory().resolve("model").resolve("IHTC_model.mzn");

		Path modelFolder = getResourceDirectory().resolve("ihtc");
		Path outputFolder = getResourceDirectory().resolve("out");

		DataModelManager manager = new DataModelManager(modelFolder, outputFolder);
		manager.loadDataModel("i01");
		manager.writeDataModelAsDZN();

		List<String> commands = new LinkedList<>();
//		commands.add(getMiniZinc().toAbsolutePath().toString());
//		commands.add("--solver");
//		commands.add("Gurobi");

		ProcessBuilder processBuilder = new ProcessBuilder(commands);
		processBuilder.directory(getUserDirectory().resolve("mini").toFile());
		processBuilder.redirectError(Redirect.INHERIT);
		processBuilder.redirectInput(Redirect.INHERIT);
		if (!jsonOutputEnabled) {
			processBuilder.redirectOutput(Redirect.INHERIT);
		}

		// path to minizinc exe
		processBuilder.command().add(getMiniZinc().toAbsolutePath().toString());

		// solver argument
		processBuilder.command().add("--solver");
		processBuilder.command().add("Gurobi");

		// constraint model
		processBuilder.command().add("--model");
		processBuilder.command().add(constraintModel.toString());

		// data model
		processBuilder.command().add("--data");
		processBuilder.command().add(manager.getPathOfOutput().toString());

		processBuilder.command().add("--time-limit");
		processBuilder.command().add(Integer.toString(2 * 60 * 1000));

		if (jsonOutputEnabled) {
			processBuilder.command().add("--json-stream");
		}

//		processBuilder.command().add("--help");

		processBuilder.command().add("--writeModel");
		processBuilder.command().add("./out.lp");

		processBuilder.command().add("-p");
		processBuilder.command().add("4");

//		var processCmd = "\"" + getMiniZinc().toAbsolutePath().toString() + "\"";
//		processBuilder.command(processCmd, getResourceDirectory().resolve("model.mpc").toString());

		CompletableFuture<Process> isDone = null;
		try {
			var miniZincProcess = processBuilder.start();
			isDone = miniZincProcess.onExit();

			if (jsonOutputEnabled) {
				var outStream = miniZincProcess.getInputStream();
				System.out.println("Found  stream: " + outStream);
				final BufferedReader outReader = new BufferedReader(new InputStreamReader(outStream));

				String lastLine = outReader.readLine();
				while (lastLine != null) {
					// json output

					var json = (JSONObject) JSON.parse(lastLine);
					System.out.println(json);
					if ("solution".equals(json.getString("type"))) {
						var output = ((JSONObject) json.get("output")).getString("dzn");
						if (output != null) {
							var solution = new MZSolution(System.currentTimeMillis(), output);
							System.out.println(solution);
						} else {
							System.out.println("Error? A");
						}
					}

					lastLine = outReader.readLine();
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (isDone != null) {
			long maxTime = 5000;
			long sleepTime = 250;
			while (!isDone.isDone() && maxTime > 0) {
				try {
					maxTime -= sleepTime;
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (!isDone.isDone()) {
				try {
					System.out.println("Process did not terminate: " + isDone.get().pid());
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			System.err.println("Error? B");
		}

	}

}
