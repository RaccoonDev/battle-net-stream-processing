/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devraccoon.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.java.ExecutionEnvironment;

import java.util.Objects;

/**
 * Skeleton for a Flink Batch Job.
 *
 * <p>For a tutorial how to write a Flink batch application, check the
 * tutorials and examples on the <a href="https://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution,
 * change the main class in the POM.xml file to this class (simply search for 'mainClass')
 * and run 'mvn clean package' on the command line.
 */
public class BatchJob {

    public static void main(String[] args) throws Exception {
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        ObjectMapper mapper = new ObjectMapper();

        env.readCsvFile("data/air-quality.csv")
                .ignoreFirstLine()
                .parseQuotedStrings('"')
                .ignoreInvalidLines()
                .types(Long.class, String.class)
                .map(t -> new Record(t.f0, t.f1))
                .map(mapper::writeValueAsString)
                .writeAsText("./out/ranks_and_cities.txt");

        env.execute("Flink Batch Java API Skeleton");
    }
}

class Record {
    private final long rank;
    private final String City;

    public Record(long rank, String city) {
        this.rank = rank;
        City = city;
    }

    public long getRank() {
        return rank;
    }

    public String getCity() {
        return City;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Record record = (Record) o;
        return rank == record.rank && Objects.equals(City, record.City);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, City);
    }

    @Override
    public String toString() {
        return "Record{" +
                "rank=" + rank +
                ", City='" + City + '\'' +
                '}';
    }
}