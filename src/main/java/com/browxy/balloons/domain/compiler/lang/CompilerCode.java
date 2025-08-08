package com.browxy.balloons.domain.compiler.lang;

import com.browxy.balloons.domain.compiler.message.Message;

public interface CompilerCode {
	public CompilerResult compileUserCode(Message message);
}
