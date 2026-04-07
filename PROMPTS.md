1. Github copilot (model: Claude Haiku 4.5)

```angular2html
Read the README.md file and write a function to read the input filename. I want to process the data using a HashMap instead of saving each line individually, and use the HashMap to summarize the results. Must follow this instruction
 - Clean, readable code — meaningful names, consistent style, no dead code or commented-out blocks
 - Error handling — handle missing files, malformed rows, and edge cases gracefully
```

```angular2html
I want cleaner code: Separate the classes, each function has its own task, and reading the file and handling the row is done by a different function, so that benchmarks can be used to replace various file reading methods.
```

```angular2html
Read three classes: MultiThreadReader (reads files in chunks by thread), SingleLineBufferReader (reads files line by line), and StreamCSVReader (reads files using byte streams). Apply this to provide multiple ways to read files instead of relying solely on bufferreaders. I will benchmark based on these different methods; for multi-threading, handle it separately to ensure consistency (each thread should have its own statistics and aggregate them upon completion).
```

```angular2html
I see that multi-threading is already working with synchronized rowHandler, which can create multiple row handlers per request. I want to modify the CSVReader interface to readFile and return a HashMap<String, CampaignStats>. Remove the synchronization from MultiThreadedCSVReader to ensure benchmarks are performed within the same context.
```

- Optimize ResultsGenerator with PriorityQUeue size with 10
- change code Main accept type of read csv (buffer, stream, thread). With option use thread, read args numberThread and pass to MultiThreadedCSVReader-
- why numThread in MultiThreadedCSVReader 8 is correct, but 16 is wrong?

4. Chat GPT 
- Is there a way to speed up the handle sort section?


3. Gemini
- I use gemini for reduce copilot quota usage
- 