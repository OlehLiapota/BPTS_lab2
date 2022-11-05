using System;
using System.Net.Http;
using System.Net.Http.Json;
using System.Security.Cryptography;
using System.Text;
using System.Windows;

namespace RSAClient
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        private const string _publicKeyUrl = "";
        private const string _loginUrl = "";

        private RSACryptoServiceProvider _rsa = new RSACryptoServiceProvider(2048);
        private string? _clientSPKI = null;
        private string? _serverSPKI = null;

        public MainWindow()
        {
            InitializeComponent();

            _clientSPKI = Convert.ToBase64String(_rsa.ExportSubjectPublicKeyInfo());
            GetPublicKey();
        }

        private async void GetPublicKey()
        {
            var httpClient = new HttpClient();

            var result = await httpClient.GetAsync(_publicKeyUrl);
            if (result.IsSuccessStatusCode)
            {
                _serverSPKI = await result.Content.ReadAsStringAsync();
            }
            else
            {
                MessageBox.Show($"Connection Error. Status Code: {result.StatusCode}");
            }
        }

        private async void Send_Click(object sender, RoutedEventArgs e)
        {
            if ((!string.IsNullOrWhiteSpace(login.Text) && !string.IsNullOrWhiteSpace(password.Text))
                && (_clientSPKI != null && _serverSPKI != null))
            {
                var loginModel = new LoginModel()
                {
                    Login = Encrypt(login.Text),
                    Password = Encrypt(password.Text),
                    Key = _clientSPKI
                };

                var httpClient = new HttpClient();
                var result = await httpClient.PostAsJsonAsync(_loginUrl, loginModel);
                if (result.IsSuccessStatusCode)
                {
                    var encryptedBytes = Convert.FromBase64String(await result.Content.ReadAsStringAsync());
                    using (var rsa = new RSACryptoServiceProvider(2048))
                    {
                        rsa.ImportPkcs8PrivateKey(_rsa.ExportPkcs8PrivateKey(), out int bytes);
                        var decryptedBytes = rsa.Decrypt(encryptedBytes, false);

                        serverMessage.Text += $"\n{Encoding.UTF8.GetString(decryptedBytes)}";
                    }
                }
                else
                {
                    MessageBox.Show($"Connection Error. Status Code: {result.StatusCode}");
                }
            }
        }

        private string Encrypt(string value)
        {
            using (var rsa = new RSACryptoServiceProvider(2048))
            {
                rsa.ImportSubjectPublicKeyInfo(Convert.FromBase64String(_serverSPKI!), out int bytes);

                var encryptedBytes = rsa.Encrypt(Encoding.UTF8.GetBytes(value), false);

                return Convert.ToBase64String(encryptedBytes);
            }
        }
    }

    public class LoginModel
    {
        public string Login { get; set; } = string.Empty;

        public string Password { get; set; } = string.Empty;

        public string Key { get; set; } = string.Empty;
    }
}
