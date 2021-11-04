package com.serverless.db;

import com.serverless.data.Message;

import java.io.IOException;
import java.util.List;

interface Adapter {

    List<Message> getMessages(String userId) throws IOException;

    void putMessage(Message message) throws IOException;

}
