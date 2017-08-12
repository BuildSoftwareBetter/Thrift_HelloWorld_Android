package com.david.thrift_helloworld_android;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class MainActivity extends AppCompatActivity {

    public class HelloWorldServiceImpl implements HelloWorldService.Iface {
        @Override
        public void SayHello(String str) throws TException {
            Message msg = new Message();
            msg.what = 0x8090;
            msg.getData().putString("msg", str);
            MainActivity.this.handler.sendMessage(msg);
        }
    }

    public class HelloWorldServe {
        public HelloWorldServiceImpl handler;

        public HelloWorldService.Processor processor;

        public void Run() {
            try {
                handler = new HelloWorldServiceImpl();
                processor = new HelloWorldService.Processor(handler);

                Runnable simple = new Runnable() {
                    public void run() {
                        simple(processor);
                    }
                };

                new Thread(simple).start();

            } catch (Exception x) {
                x.printStackTrace();
            }
        }

        public void simple(HelloWorldService.Processor processor) {
            try {

                final EditText etPort = (EditText) findViewById(R.id.edtSrvPort);
                String port = etPort.getText().toString();
                int p = Integer.parseInt(port);

                TServerTransport serverTransport = new TServerSocket(p);
                //TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));

                // Use this for a multithreaded server
                TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

                System.out.println("Starting the simple server...");

                Message msg = new Message();
                msg.what = 0x8090;
                msg.getData().putString("msg", "Starting");
                MainActivity.this.handler.sendMessage(msg);
                server.serve();
            } catch (Exception e) {
                Message msg = new Message();
                msg.what = 0x8090;
                msg.getData().putString("msg", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public class HelloWorldClient {
        public int Port;
        public String IP;
        public String Msg;

        void CallServer(String ip, int port, String msg) throws TException {
            Port = port;
            IP = ip;
            Msg = msg;

            Runnable simple = new Runnable() {
                public void run() {
                    TTransport transport = new TSocket(IP, Port);
                    try {

                        transport.open();
                        TProtocol protocol = new TBinaryProtocol(transport);
                        HelloWorldService.Client client = new HelloWorldService.Client(protocol);
                        client.SayHello(Msg);
                    } catch (TException ex) {
                        Message msg = new Message();
                        msg.what = 0x8090;
                        msg.getData().putString("msg", ex.getMessage());
                        MainActivity.this.handler.sendMessage(msg);
                    } finally {
                        transport.close();
                    }
                }
            };

            new Thread(simple).start();
/*
            AsyncTask<Void, String, Void> read = new AsyncTask<Void, String, Void>() {

                @Override
                protected Void doInBackground(Void... arg0) {
                    TTransport transport = new TSocket(IP, Port);
                    try {

                        transport.open();
                        TProtocol protocol = new TBinaryProtocol(transport);
                        HelloWorldService.Client client = new HelloWorldService.Client(protocol);
                        client.SayHello(Msg);
                    } catch (TException ex) {
                        Message msg = new Message();
                        msg.what = 0x8090;
                        msg.getData().putString("msg", ex.getMessage());
                        MainActivity.this.handler.sendMessage(msg);
                    } finally {
                        transport.close();
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(String... values) {
                    if (values[0].equals("@success")) {
                        Toast.makeText(MainActivity.this, "链接成功！", Toast.LENGTH_SHORT).show();
                    }
                    super.onProgressUpdate(values);
                }

            };
            read.execute();

            */
        }
    }

    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x8090) {
                String str = (String) msg.getData().get("msg");
                final TextView tvMsg = (TextView) findViewById(R.id.tvMsg);
                tvMsg.setText(str);//Activity上的TextView元素显示消息内容
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btnServer = (Button) findViewById(R.id.btnStart);
        btnServer.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             HelloWorldServe srv = new HelloWorldServe();
                                             srv.Run();
                                             btnServer.setEnabled(false);
                                         }
                                     }
        );

        final Button btn = (Button) findViewById(R.id.btnSend);
        //设置点击后TextView现实的内容
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                try {
                    final EditText cPort = (EditText) findViewById(R.id.edtCliPort);
                    String port = cPort.getText().toString();
                    int p = Integer.parseInt(port);

                    final EditText cIp = (EditText) findViewById(R.id.edtCliIP);
                    String ip = cIp.getText().toString();

                    final EditText cMsg = (EditText) findViewById(R.id.edtMsg);
                    String msg = cMsg.getText().toString();

                    HelloWorldClient cli = new HelloWorldClient();
                    cli.CallServer(ip, p, msg);

                } catch (TException ex) {
                    Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
