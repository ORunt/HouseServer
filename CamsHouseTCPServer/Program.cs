using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Collections;

namespace CamsHouseTCPServer
{
    public class MultiThreadedServer
    {
        // Light state.
        private static byte lightState;
        // List of all the currently connected clients.
        private static Hashtable clientList = new Hashtable();

        public static void Main()
        {
            int serverPort = 8080;
            string serverIP = "192.168.3.68";
            TcpListener listener = null;
            TcpClient clientSocket = default(TcpClient);
            try
            {
                // Initialize all the light states to OFF.
                lightState = 0x00;
                // Start port listener.
                listener = new TcpListener(IPAddress.Parse(serverIP), serverPort);
                listener.Start();
                Console.WriteLine("CamsHouseTCPServer started...");
                while (true)
                {
                    Console.WriteLine("Waiting for incoming client connections...");
                    // Accept new client connection.
                    clientSocket = listener.AcceptTcpClient();                          // Will pause here until a new client tries to connect to the server.
                    // Add new client connection to list.
                    clientList.Add("User " + clientList.Count, clientSocket);           // TODO: add user names...
                    Console.WriteLine("Accepted new client connection...");
                    // Create new client handler on separate thread.
                    ClientHandler client = new ClientHandler(clientSocket, clientList, ref lightState);
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }
            finally
            {
                if (listener != null)
                {
                    listener.Stop();
                }
            }
        }

        public static void Broadcast()
        {
            TcpClient broadcastSocket = null;
            NetworkStream broadcastStream = null;

            foreach (DictionaryEntry client in clientList)
            {
                broadcastSocket = (TcpClient)client.Value;
                broadcastStream = broadcastSocket.GetStream();
                byte[] broadcastBytes = BitConverter.GetBytes(lightState);
                broadcastStream.Write(broadcastBytes, 0, 1);                        // TODO: increase size if needed.
                broadcastStream.Flush();
            }
        }

    }

    public class ClientHandler
    {
        TcpClient client;
        Hashtable clientList;
        uint lightState;

        public ClientHandler(TcpClient cSocket, Hashtable cList, ref byte lState)
        {
            lightState = lState;
            client = cSocket;
            clientList = cList;
            Thread clientThread = new Thread(ProcessClientRequests);
            clientThread.Start();
        }

        public void ProcessClientRequests()
        {
            byte[] dataBuffer = new byte[client.ReceiveBufferSize];

            try
            {
                // Create network stream for client communication.
                NetworkStream clientStream = client.GetStream();
                // Send current light state to client.
                dataBuffer = BitConverter.GetBytes(lightState);
                clientStream.Write(dataBuffer, 0, 1);
                clientStream.Flush();
                while (true)
                {
                    clientStream.Read(dataBuffer, 0, dataBuffer.Length);
                    if (dataBuffer[0] == 0xFF)
                        break;
                    lightState = dataBuffer[0];
                    Console.WriteLine("From client -> 0x" + lightState.ToString("X2"));
                    // Update all the threads with the new light state.
                    MultiThreadedServer.Broadcast();
                }
                clientStream.Close();
                client.Close();
                Console.WriteLine("Closing client connection.");
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }
            finally
            {
                if (client != null)
                {
                    client.Close();
                }
            }
        }
    }
}
