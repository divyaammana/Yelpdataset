package WordCount.WordCount;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Q3 {
	public static class BusinessMap extends Mapper<LongWritable, Text, Text, IntWritable> {

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			// from business
			String delims = "^";
			String[] businessData = StringUtils.split(value.toString(), delims);

			if (businessData.length == 3) {
				String str = businessData[1];
				String str1 = str.substring(str.length() - 5);
				if (str1.matches("[0-9]{5}")) {
					context.write(new Text(str1), new IntWritable(1));
				}
			}
		}

	}

	public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {
		private Map<Text, IntWritable> cmap = new HashMap<Text, IntWritable>();

		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {

			int count = 0;
			for (IntWritable t : values) {
				count++;
			}

			cmap.put(new Text(key), new IntWritable(count));
			

		}

		protected void cleanup(Context context) throws IOException, InterruptedException {
			Map<Text, IntWritable> smap = sortByValues(cmap);
			int count = 0;
			for (Text key : smap.keySet()) {
				count++;
				if (count == 11)
					break;
				context.write(key, smap.get(key));

			}

		}
	}

	private static <K extends Comparable, V extends Comparable> Map<K, V> sortByValues(Map<K, V> map) {
		List<Map.Entry<K, V>> entries = new LinkedList<Map.Entry<K, V>>(map.entrySet());

		Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {

				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		Map<K, V> smap = new LinkedHashMap<K, V>();

		for (Map.Entry<K, V> entry : entries) {
			smap.put(entry.getKey(), entry.getValue());
		}

		return smap;
	}

	// Driver program
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs(); // get
																						// all
																						// args
		if (otherArgs.length != 2) {
			System.err.println("Usage: CountYelpBusiness <in> <out>");
			System.exit(2);
		}

		Job job = Job.getInstance(conf, "CountYelp");
		job.setJarByClass(Q3.class);

		job.setMapperClass(BusinessMap.class);
		job.setReducerClass(Reduce.class);
		// uncomment the following line to add the Combiner
		// job.setCombinerClass(Reduce.class);

		// set output key type

		job.setOutputKeyClass(Text.class);

		// set output value type
		job.setMapOutputValueClass(IntWritable.class);
		job.setOutputValueClass(IntWritable.class);

		// set the HDFS path of the input data
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		// set the HDFS path for the output
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

		// Wait till job completion
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
