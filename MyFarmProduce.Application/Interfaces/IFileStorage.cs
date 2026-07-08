namespace MyFarmProduce.Application.Interfaces;

public interface IFileStorage
{
    /// <summary>Saves an uploaded image under the given folder and returns its public relative URL.</summary>
    Task<string> SaveImageAsync(Stream content, string originalFileName, string folder);
}
