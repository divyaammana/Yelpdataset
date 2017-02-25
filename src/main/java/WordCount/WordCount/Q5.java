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
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Q5 {
	public static class BusinessMap extends Mapper<LongWritable, Text, Text, FloatWritable> {

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			// from business
			String delims = "^";
			String[] reviewData = StringUtils.split(value.toString(), delims);
			String str = reviewData[2];
			float i = Float.parseFloat(reviewData[3]);
			
				context.write(new Text(str), new FloatWritable(i));

			
		}

	}

	public static class Reduce extends Reducer<Text, FloatWritable, Text, FloatWritable> {
		private Map<Text, FloatWritable> cmap = new HashMap<Text, FloatWritable>();

		public void reduce(Text key, Iterable<FloatWritable> values, Context context)
				throws IOException, InterruptedException {

			float sum = 0;
			int count = 0;
			for (FloatWritable t : values) {
				sum = +t.get();
				count++;
			}
			float avg = (sum/count);
			cmap.put(new Text(key), new FloatWritable(avg));

		}

		protected void cleanup(Context context) throws IOException, InterruptedException {
			Map<Text, FloatWritable> smap = sortByValues(cmap);
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
				return o1.getValue().compareTo(o2.getValue());
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
		job.setJarByClass(Q5.class);

		job.setMapperClass(BusinessMap.class);
		job.setReducerClass(Reduce.class);
		// uncomment the following line to add the Combiner
		// job.setCombinerClass(Reduce.class);

		// set output key type

		job.setOutputKeyClass(Text.class);

		// set output value type
		job.setMapOutputValueClass(FloatWritable.class);
		job.setOutputValueClass(FloatWritable.class);

		// set the HDFS path of the input data
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		// set the HDFS path for the output
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

		// Wait till job completion
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
