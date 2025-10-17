package com.study.FlowTrack;

import org.springframework.boot.SpringApplication;

public class TestFlowTrackApplication {

	public static void main(String[] args) {
		SpringApplication.from(FlowTrackApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
