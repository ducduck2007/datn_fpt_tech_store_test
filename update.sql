IF OBJECT_ID('dbo.system_settings', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.system_settings (
        setting_key   NVARCHAR(50) NOT NULL PRIMARY KEY,
        setting_value NVARCHAR(100) NOT NULL,
        updated_at    DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.system_settings WHERE setting_key = 'DEFAULT_CURRENCY')
BEGIN
    INSERT INTO dbo.system_settings(setting_key, setting_value) VALUES ('DEFAULT_CURRENCY', 'VND');
END
GO

ALTER TABLE dbo.user_logins
ADD updated_at DATETIME2 NOT NULL
    CONSTRAINT DF_user_logins_updated_at DEFAULT SYSDATETIME();
GO

ALTER TABLE user_logins
ALTER COLUMN updated_at DATETIME2 NULL;
GO
